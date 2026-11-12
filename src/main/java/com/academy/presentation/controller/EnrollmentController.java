package com.academy.presentation.controller;

import com.academy.business.service.AcademyService;
import com.academy.presentation.dto.ApiDtos.EnrollmentRequest;
import com.academy.presentation.dto.ApiDtos.EnrollmentResponse;
import com.academy.presentation.mapper.ApiMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final AcademyService service;

    public EnrollmentController(AcademyService service) { this.service = service; }

    @GetMapping("/{id}")
    public EnrollmentResponse findById(@PathVariable Long id) {
        return ApiMapper.toResponse(service.findEnrollment(id));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = ApiMapper.toResponse(service.enroll(request.studentId(), request.courseId()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
}