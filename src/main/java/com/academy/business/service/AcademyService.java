package com.academy.business.service;

import com.academy.business.exception.ApiExceptions;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.EnrollmentEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.repository.CourseJpaRepository;
import com.academy.persistence.repository.EnrollmentJpaRepository;
import com.academy.persistence.repository.StudentJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyService {

    private final StudentJpaRepository students;
    private final CourseJpaRepository courses;
    private final EnrollmentJpaRepository enrollments;

    public AcademyService(StudentJpaRepository students, CourseJpaRepository courses,
                          EnrollmentJpaRepository enrollments) {
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @Transactional(readOnly = true)
    public Page<StudentEntity> findStudents(Pageable pageable) {
        return students.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public StudentEntity findStudent(Long id) {
        return students.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("No existe un estudiante con id " + id));
    }

    @Transactional(readOnly = true)
    public StudentEntity findStudentByEmail(String email) {
        return students.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiExceptions.NotFound("No existe un estudiante con el correo " + email));
    }

    @Transactional
    public StudentEntity createStudent(StudentEntity student) {
        if (students.existsByEmailIgnoreCase(student.getEmail())) {
            throw new ApiExceptions.Conflict("El correo " + student.getEmail() + " ya está registrado");
        }
        try {
            return students.saveAndFlush(student);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiExceptions.Conflict("El correo " + student.getEmail() + " ya está registrado");
        }
    }

    @Transactional(readOnly = true)
    public Page<CourseEntity> findCourses(Pageable pageable) {
        return courses.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public CourseEntity findCourse(Long id) {
        return courses.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("No existe un curso con id " + id));
    }

    @Transactional
    public CourseEntity createCourse(CourseEntity course) {
        return courses.save(course);
    }

    @Transactional(readOnly = true)
    public long countEnrolled(Long courseId) {
        if (!courses.existsById(courseId)) {
            throw new ApiExceptions.NotFound("No existe un curso con id " + courseId);
        }
        return enrollments.countByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public EnrollmentEntity findEnrollment(Long id) {
        return enrollments.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("No existe una matrícula con id " + id));
    }

    @Transactional
    public EnrollmentEntity enroll(Long studentId, Long courseId) {
        StudentEntity student = students.findById(studentId)
                .orElseThrow(() -> new ApiExceptions.NotFound("No existe un estudiante con id " + studentId));
        CourseEntity course = courses.findByIdForUpdate(courseId)
                .orElseThrow(() -> new ApiExceptions.NotFound("No existe un curso con id " + courseId));
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ApiExceptions.Conflict("El estudiante " + studentId + " ya está matriculado en el curso " + courseId);
        }
        long enrolled = enrollments.countByCourseId(courseId);
        if (enrolled >= course.getMaxCapacity()) {
            throw new ApiExceptions.Conflict("El curso " + courseId + " ya alcanzó su cupo de " + course.getMaxCapacity());
        }
        try {
            return enrollments.saveAndFlush(new EnrollmentEntity(student, course));
        } catch (DataIntegrityViolationException ex) {
            throw new ApiExceptions.Conflict("El estudiante " + studentId + " ya está matriculado en el curso " + courseId);
        }
    }
}