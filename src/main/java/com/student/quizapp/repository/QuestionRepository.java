package com.student.quizapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.student.quizapp.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByChapterId(Long chapterId);
}