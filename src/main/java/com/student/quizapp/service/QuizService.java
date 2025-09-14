package com.student.quizapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.student.quizapp.model.Chapter;
import com.student.quizapp.model.Question;
import com.student.quizapp.repository.ChapterRepository;
import com.student.quizapp.repository.QuestionRepository;

@Service
public class QuizService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Value("${llm_api_key:default-api-key}")
    private String llmApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Question> getRandomQuestions(Long chapterId, int count, String subjectName, String chapterName) {
        if (chapterId == null || count <= 0) {
            throw new IllegalArgumentException("Invalid chapterId or count");
        }

        List<Question> existingQuestions = questionRepository.findByChapterId(chapterId);
        if (existingQuestions == null) {
            existingQuestions = new ArrayList<>();
        }
        System.out.println("Found " + existingQuestions.size() + " existing questions for chapterId: " + chapterId);

        if (existingQuestions.size() >= count) {
            System.out.println("Returning " + count + " existing questions for chapterId: " + chapterId);
            return existingQuestions.subList(0, count);
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chapter ID: " + chapterId));
        String chapterContent = chapter.getContent();
        if (chapterContent == null || chapterContent.isEmpty()) {
            throw new IllegalArgumentException("No content available for chapter: " + chapterName);
        }

        List<Question> newQuestions = generateQuestionsFromLLM(chapterContent, chapter, count - existingQuestions.size());
        System.out.println("Generated " + newQuestions.size() + " new questions for chapterId: " + chapterId);

        if (!newQuestions.isEmpty()) {
            questionRepository.saveAll(newQuestions);
        }

        List<Question> allQuestions = new ArrayList<>(existingQuestions);
        allQuestions.addAll(newQuestions);
        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("No questions available for chapter: " + chapterName);
        }
        return allQuestions.subList(0, Math.min(count, allQuestions.size()));
    }

    private List<Question> generateQuestionsFromLLM(String content, Chapter chapter, int count) {
        if ("default-api-key".equals(llmApiKey)) {
            System.out.println("Skipping LLM call in test environment");
            return new ArrayList<>();
        }

        String prompt = "Generate " + count + " multiple-choice questions based on the following content. " +
                "Each question should have a question text, four options (labeled Option 1, Option 2, Option 3, Option 4), " +
                "and a correct answer (indicating which option is correct). Format each question as follows:\n" +
                "Question: [Your question here]\n" +
                "Option 1: [Option text]\n" +
                "Option 2: [Option text]\n" +
                "Option 3: [Option text]\n" +
                "Option 4: [Option text]\n" +
                "Correct Answer: Option [1-4]\n\n" +
                "Content:\n" + content;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + llmApiKey;
        String requestBody = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}]}]}";

        try {
            String response = restTemplate.postForObject(url, requestBody, String.class);
            System.out.println("Gemini API response: " + response);
            return parseLLMResponse(response, chapter);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Question> parseLLMResponse(String response, Chapter chapter) {
        List<Question> questions = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            return questions;
        }

        String[] questionBlocks = response.split("\\n\\n");
        Pattern questionPattern = Pattern.compile(
                "Question: (.*?)\\n" +
                "Option 1: (.*?)\\n" +
                "Option 2: (.*?)\\n" +
                "Option 3: (.*?)\\n" +
                "Option 4: (.*?)\\n" +
                "Correct Answer: Option (\\d)",
                Pattern.DOTALL);

        for (String block : questionBlocks) {
            Matcher matcher = questionPattern.matcher(block.trim());
            if (matcher.find()) {
                Question question = new Question();
                question.setQuestionText(matcher.group(1).trim());
                question.setOption1(matcher.group(2).trim());
                question.setOption2(matcher.group(3).trim());
                question.setOption3(matcher.group(4).trim());
                question.setOption4(matcher.group(5).trim());
                question.setCorrectAnswer("Option " + matcher.group(6));
                question.setChapter(chapter);
                questions.add(question);
            } else {
                System.err.println("Failed to parse question block: " + block);
            }
        }

        return questions;
    }

    public int calculateScore(List<Question> questions, Map<Long, String> userAnswers) {
        int score = 0;
        for (Question question : questions) {
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                score++;
            }
        }
        return score;
    }

}