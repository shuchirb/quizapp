package com.student.quizapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.student.quizapp.model.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySubjectId(Long subjectId);
}