package com.student.quizapp.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.student.quizapp.model.Chapter;
import com.student.quizapp.model.Question;
import com.student.quizapp.repository.ChapterRepository;
import com.student.quizapp.repository.QuestionRepository;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = QuizService.class)
class QuizServiceTest {

    @InjectMocks
    private QuizService quizService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Test
    void testGetRandomQuestions_withExistingQuestions() {
        // Arrange
        Long chapterId = 3L;
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setName("Physics");
        chapter.setContent("Physics is the study of matter and energy.");

        Question question = new Question();
        question.setId(1L);
        question.setQuestionText("What is Newton's First Law?");
        question.setOption1("Object at rest stays at rest");
        question.setOption2("Force equals mass times acceleration");
        question.setOption3("Action-reaction");
        question.setOption4("Energy conservation");
        question.setCorrectAnswer("Option 1");
        question.setChapter(chapter);

        when(chapterRepository.findById(chapterId)).thenReturn(java.util.Optional.of(chapter));
        when(questionRepository.findByChapterId(chapterId)).thenReturn(Arrays.asList(question));

        // Act
        List<Question> questions = quizService.getRandomQuestions(chapterId, 1, "Science", "Physics");

        // Assert
        assertEquals(1, questions.size());
        assertEquals("What is Newton's First Law?", questions.get(0).getQuestionText());
        verify(questionRepository, times(1)).findByChapterId(chapterId);
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetRandomQuestions_noExistingQuestions() {
        // Arrange
        Long chapterId = 3L;
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setName("Physics");
        chapter.setContent("Physics is the study of matter and energy.");

        when(chapterRepository.findById(chapterId)).thenReturn(java.util.Optional.of(chapter));
        when(questionRepository.findByChapterId(chapterId)).thenReturn(Collections.emptyList());

        // Act
        List<Question> questions = quizService.getRandomQuestions(chapterId, 1, "Science", "Physics");

        // Assert
        assertEquals(0, questions.size()); // No questions generated due to default API key
        verify(questionRepository, times(1)).findByChapterId(chapterId);
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetRandomQuestions_invalidChapterId() {
        // Arrange
        Long chapterId = 999L;
        when(chapterRepository.findById(chapterId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                quizService.getRandomQuestions(chapterId, 1, "Science", "Physics"));
        assertEquals("Invalid chapter ID: " + chapterId, exception.getMessage());
    }
}