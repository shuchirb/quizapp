package com.student.quizapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "CLASS_ENTITY")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subject> subjects;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "ClassEntity{id=" + id + ", name='" + name + "'}";
    }
}