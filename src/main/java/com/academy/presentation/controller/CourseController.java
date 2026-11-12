package com.academy.presentation.controller;

import com.academy.business.service.AcademyService;
import com.academy.presentation.dto.ApiDtos.CourseRequest;
import com.academy.presentation.dto.ApiDtos.CourseResponse;
import com.academy.presentation.dto.ApiDtos.EnrolledResponse;
import com.academy.presentation.dto.ApiDtos.PageResponse;
import com.academy.presentation.mapper.ApiMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final AcademyService service;

    public CourseController(AcademyService service) { this.service = service; }

    @GetMapping
    public PageResponse<CourseResponse> findAll(Pageable pageable) {
        return ApiMapper.toPageResponse(service.findCourses(pageable), ApiMapper::toResponse);
    }

    @GetMapping("/{id}")
    public CourseResponse findById(@PathVariable Long id) {
        return ApiMapper.toResponse(service.findCourse(id));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = ApiMapper.toResponse(service.createCourse(ApiMapper.toEntity(request)));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}/enrolled")
    public EnrolledResponse countEnrolled(@PathVariable Long id) {
        return new EnrolledResponse(id, service.countEnrolled(id));
    }
}