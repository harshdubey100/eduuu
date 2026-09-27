package com.eduverse.backend.repository;

import com.eduverse.backend.Entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // ✅ ADDED: Automatically retrieves all enrollment rows matching a specific student's ID
    List<Enrollment> findByStudentId(Long studentId);

    // Used for limited-seat capacity checks
    long countByCourseId(Long courseId);
}
