package com.student.quizapp.repository;

import com.student.quizapp.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
}