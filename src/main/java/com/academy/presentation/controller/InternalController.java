package com.academy.presentation.controller;

import com.academy.business.service.AcademyService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.StudentEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final AcademyService service;

    public InternalController(AcademyService service) { this.service = service; }

    @SecurityRequirements
    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP"); }

    @PostMapping("/seed")
    public Map<String, Integer> seed() {
        int studentCount = 0;
        StudentEntity[] samples = {
                new StudentEntity("Ana Torres", 22, "ana@mail.com"),
                new StudentEntity("Luis Perez", 25, "luis@mail.com"),
                new StudentEntity("Maria Gomez", 31, "maria@mail.com")
        };
        for (StudentEntity sample : samples) {
            try {
                service.createStudent(sample);
                studentCount++;
            } catch (com.academy.business.exception.ApiExceptions.Conflict ignored) {
                // Sembrar varias veces no duplica estudiantes.
            }
        }
        int courseCount = 0;
        if (service.findCourses(PageRequest.of(0, 1)).isEmpty()) {
            service.createCourse(new CourseEntity("Java desde cero", 150000, 30));
            service.createCourse(new CourseEntity("Spring Boot", 250000, 20));
            courseCount = 2;
        }
        return Map.of("students", studentCount, "courses", courseCount);
    }
}