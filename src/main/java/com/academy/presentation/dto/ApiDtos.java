package com.academy.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() { }

    public record StudentRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @Min(16) @Max(99) int age,
            @NotBlank @Email String email) { }

    public record CourseRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @Positive double price,
            @Min(1) int maxCapacity) { }

    public record EnrollmentRequest(@NotNull Long studentId, @NotNull Long courseId) { }
    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    public record StudentResponse(Long id, String name, int age, String email) { }
    public record CourseResponse(Long id, String name, double price, int maxCapacity) { }
    public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseName) { }
    public record EnrolledResponse(Long courseId, long enrolled) { }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
    public record LoginResponse(String token, String tokenType, long expiresIn) { }
    public record MeResponse(String username, String role) { }
    public record FieldIssue(String field, String message) { }
    public record ErrorResponse(Instant timestamp, int status, String error, String message, String path,
                               List<FieldIssue> details) { }
}