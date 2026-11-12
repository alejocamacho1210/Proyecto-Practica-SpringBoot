package com.academy.persistence.repository;

import com.academy.persistence.entity.CourseEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseEntity c where c.id = :id")
    Optional<CourseEntity> findByIdForUpdate(@Param("id") Long id);
}