package com.student.quizapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.student.quizapp.model.Chapter;
import com.student.quizapp.model.ClassEntity;
import com.student.quizapp.model.Subject;
import com.student.quizapp.repository.ChapterRepository;
import com.student.quizapp.repository.ClassRepository;
import com.student.quizapp.repository.QuestionRepository;
import com.student.quizapp.repository.SubjectRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running DataInitializer...");

        // Clear existing data
        questionRepository.deleteAll();
        chapterRepository.deleteAll();
        subjectRepository.deleteAll();
        classRepository.deleteAll();

        // Create Class
        ClassEntity class10 = new ClassEntity();
        class10.setName("10th Grade");
        class10 = classRepository.save(class10);
        System.out.println("Saved Class: id=" + class10.getId() + ", name=" + class10.getName());

        // Create Math Subject
        Subject math = new Subject();
        math.setName("Math");
        math.setClassEntity(class10);
        math = subjectRepository.save(math);
        System.out.println("Saved Subject: id=" + math.getId() + ", name=" + math.getName());

        // Create Math Chapters
        Chapter algebra = new Chapter();
        algebra.setName("Algebra");
        algebra.setSubject(math);
        algebra.setContent("Algebra involves solving equations with variables. Key concepts include linear equations, quadratic equations, and polynomials.");
        algebra = chapterRepository.save(algebra);
        System.out.println("Saved Chapter: id=" + algebra.getId() + ", name=" + algebra.getName());

        Chapter geometry = new Chapter();
        geometry.setName("Geometry");
        geometry.setSubject(math);
        geometry.setContent("Geometry studies shapes, sizes, and properties of figures. Topics include triangles, circles, and coordinate geometry.");
        geometry = chapterRepository.save(geometry);
        System.out.println("Saved Chapter: id=" + geometry.getId() + ", name=" + geometry.getName());

        // Create Science Subject部分

        Subject science = new Subject();
        science.setName("Science");
        science.setClassEntity(class10);
        science = subjectRepository.save(science);
        System.out.println("Saved Subject: id=" + science.getId() + ", name=" + science.getName());

        Chapter physics = new Chapter();
        physics.setName("Physics");
        physics.setSubject(science);
        physics.setContent("Physics is the study of matter and energy. Key topics include motion, forces, and Newton's laws. For example, Newton's First Law states that an object at rest stays at rest, and an object in motion stays in motion unless acted upon by an external force.");
        physics = chapterRepository.save(physics);
        System.out.println("Saved Chapter: id=" + physics.getId() + ", name=" + physics.getName());

        Chapter chemistry = new Chapter();
        chemistry.setName("Chemistry");
        chemistry.setSubject(science);
        chemistry.setContent("Chemistry explores the composition and properties of matter. Core concepts include atoms, molecules, and chemical reactions, such as the reaction between hydrogen and oxygen to form water.");
        chemistry = chapterRepository.save(chemistry);
        System.out.println("Saved Chapter: id=" + chemistry.getId() + ", name=" + chemistry.getName());
    }
}