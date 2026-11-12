package com.academy.presentation.mapper;

import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.presentation.dto.ApiDtos.CourseRequest;
import com.academy.presentation.dto.ApiDtos.CourseResponse;
import com.academy.presentation.dto.ApiDtos.EnrollmentResponse;
import com.academy.presentation.dto.ApiDtos.PageResponse;
import com.academy.presentation.dto.ApiDtos.StudentRequest;
import com.academy.presentation.dto.ApiDtos.StudentResponse;
import org.springframework.data.domain.Page;

public final class ApiMapper {

    private ApiMapper() { }

    public static StudentEntity toEntity(StudentRequest request) {
        return new StudentEntity(request.name(), request.age(), request.email());
    }

    public static StudentResponse toResponse(StudentEntity entity) {
        return new StudentResponse(entity.getId(), entity.getName(), entity.getAge(), entity.getEmail());
    }

    public static CourseEntity toEntity(CourseRequest request) {
        return new CourseEntity(request.name(), request.price(), request.maxCapacity());
    }

    public static CourseResponse toResponse(CourseEntity entity) {
        return new CourseResponse(entity.getId(), entity.getName(), entity.getPrice(), entity.getMaxCapacity());
    }

    public static EnrollmentResponse toResponse(EnrollmentEntity entity) {
        return new EnrollmentResponse(entity.getId(), entity.getStudent().getId(), entity.getStudent().getName(),
                entity.getCourse().getId(), entity.getCourse().getName());
    }

    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, java.util.function.Function<E, D> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}