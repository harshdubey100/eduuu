package com.eduverse.backend.repository;

import com.eduverse.backend.Entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    List<LessonProgress> findByStudentIdAndLessonModuleCourseId(Long studentId, Long courseId);
    long countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(Long studentId, Long courseId);
}
