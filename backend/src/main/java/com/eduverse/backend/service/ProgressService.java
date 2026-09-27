package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Lesson;
import com.eduverse.backend.Entity.LessonProgress;
import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.CourseProgressResponseDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.exception.ResourceNotFoundException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.LessonProgressRepository;
import com.eduverse.backend.repository.LessonRepository;
import com.eduverse.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ProgressService(LessonProgressRepository lessonProgressRepository,
                            LessonRepository lessonRepository,
                            UserRepository userRepository,
                            CourseRepository courseRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // Mark a single lesson complete for the logged-in student
    public void markLessonComplete(Long lessonId) {
        User student = getLoggedInStudent();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson with ID " + lessonId + " not found"));

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseGet(() -> {
                    LessonProgress p = new LessonProgress();
                    p.setStudent(student);
                    p.setLesson(lesson);
                    return p;
                });

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);

        log.info("action=lesson_completed studentId={} lessonId={}", student.getId(), lessonId);
    }

    // Get the logged-in student's completion percentage for a course
    public CourseProgressResponseDTO getCourseProgress(Long courseId) {
        User student = getLoggedInStudent();

        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + courseId + " not found"));

        var lessonsInCourse = lessonRepository.findByModuleCourseId(courseId);
        int totalLessons = lessonsInCourse.size();

        long completedLessons = lessonProgressRepository
                .countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(student.getId(), courseId);

        double percentage = totalLessons == 0 ? 0.0 : (completedLessons * 100.0) / totalLessons;

        CourseProgressResponseDTO dto = new CourseProgressResponseDTO();
        dto.setCourseId(courseId);
        dto.setCourseTitle(course.getTitle());
        dto.setTotalLessons(totalLessons);
        dto.setCompletedLessons((int) completedLessons);
        dto.setProgressPercentage(Math.round(percentage * 100.0) / 100.0);
        dto.setCourseCompleted(totalLessons > 0 && completedLessons == totalLessons);

        return dto;
    }

    private User getLoggedInStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
    }
}
