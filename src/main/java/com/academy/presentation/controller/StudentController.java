package com.academy.presentation.controller;

import com.academy.business.service.AcademyService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.presentation.dto.ApiDtos.PageResponse;
import com.academy.presentation.dto.ApiDtos.StudentRequest;
import com.academy.presentation.dto.ApiDtos.StudentResponse;
import com.academy.presentation.mapper.ApiMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final AcademyService service;

    public StudentController(AcademyService service) { this.service = service; }

    @GetMapping
    public PageResponse<StudentResponse> findAll(Pageable pageable) {
        return ApiMapper.toPageResponse(service.findStudents(pageable), ApiMapper::toResponse);
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable Long id) {
        return ApiMapper.toResponse(service.findStudent(id));
    }

    @GetMapping("/by-email")
    public StudentResponse findByEmail(@RequestParam String email) {
        return ApiMapper.toResponse(service.findStudentByEmail(email));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = ApiMapper.toResponse(service.createStudent(ApiMapper.toEntity(request)));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
}