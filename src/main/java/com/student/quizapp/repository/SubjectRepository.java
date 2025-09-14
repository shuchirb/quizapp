package com.student.quizapp.repository;

import com.student.quizapp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassEntityId(Long classId);
}