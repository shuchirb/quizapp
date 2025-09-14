package com.student.quizapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.quizapp.model.Chapter;
import com.student.quizapp.model.ClassEntity;
import com.student.quizapp.model.Question;
import com.student.quizapp.model.Subject;
import com.student.quizapp.repository.ChapterRepository;
import com.student.quizapp.repository.ClassRepository;
import com.student.quizapp.repository.SubjectRepository;
import com.student.quizapp.service.QuizService;

@Controller
public class QuizController {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private QuizService quizService;

    @GetMapping("/")
    public String home(Model model) {
        List<ClassEntity> classes = classRepository.findAll();
        model.addAttribute("classes", classes);
        return "index";
    }

    @GetMapping("/subjects")
    @ResponseBody
    public List<Subject> getSubjects(@RequestParam Long classId) {
        System.out.println("Fetching subjects for classId: " + classId);
        List<Subject> subjects = subjectRepository.findByClassEntityId(classId);
        System.out.println("Subjects found: " + subjects.size());
        return subjects;
    }

 @GetMapping("/chapters")
@ResponseBody
public List<Chapter> getChapters(@RequestParam Long subjectId) {
    System.out.println("Fetching chapters for subjectId: " + subjectId);
    List<Chapter> chapters = chapterRepository.findBySubjectId(subjectId);
    System.out.println("Chapters found: " + chapters.size());
    return chapters;
}

    @PostMapping("/create-test")
    public String createTest(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (classId == null || subjectId == null || chapterId == null) {
            redirectAttributes.addFlashAttribute("error", "Please select a class, subject, and chapter.");
            return "redirect:/";
        }
        try {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID"));
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid chapter ID"));
            List<Question> questions = quizService.getRandomQuestions(chapterId, 5, subject.getName(), chapter.getName());
            model.addAttribute("questions", questions);
            model.addAttribute("chapterId", chapterId);
            model.addAttribute("subjectName", subject.getName());
            model.addAttribute("chapterName", chapter.getName());
            return "test";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/submit-test")
    public String submitTest(
            @RequestParam Map<String, String> params,
            Model model,
            RedirectAttributes redirectAttributes) throws NumberFormatException {
        try {
            Long chapterId = Long.parseLong(params.get("chapterId"));
            String subjectName = params.get("subjectName");
            String chapterName = params.get("chapterName");
            if (chapterId == null || subjectName == null || chapterName == null) {
                redirectAttributes.addFlashAttribute("error", "Missing required parameters.");
                return "redirect:/";
            }
            List<Question> questions = quizService.getRandomQuestions(chapterId, 5, subjectName, chapterName);
            Map<Long, String> userAnswers = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("answer_")) {
                    Long qId = Long.parseLong(entry.getKey().substring(7));
                    userAnswers.put(qId, entry.getValue());
                }
            }
            int score = quizService.calculateScore(questions, userAnswers);
            model.addAttribute("score", score);
            model.addAttribute("total", questions.size());
            model.addAttribute("questions", questions);
            model.addAttribute("userAnswers", userAnswers);
            return "result";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid input: " + e.getMessage());
            return "redirect:/";
        }
    }
    @PostMapping("/upload-pdf")
public String uploadPdf(@RequestParam Long chapterId, @RequestParam MultipartFile pdfFile, RedirectAttributes redirectAttributes) {
    try {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chapter ID: " + chapterId));

        // Extract text from PDF
        byte[] pdfBytes = pdfFile.getBytes();
PDDocument document = Loader.loadPDF(pdfBytes);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();

        chapter.setContent(text);
        chapterRepository.save(chapter);

        redirectAttributes.addFlashAttribute("success", "PDF uploaded and text extracted successfully!");
        return "redirect:/";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Failed to upload PDF: " + e.getMessage());
        return "redirect:/";
    }
}
}