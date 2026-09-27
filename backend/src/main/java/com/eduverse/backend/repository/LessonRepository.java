package com.eduverse.backend.repository;

import com.eduverse.backend.Entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderBySequenceAsc(Long moduleId);
    List<Lesson> findByModuleCourseId(Long courseId);
}
