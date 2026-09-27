package com.eduverse.backend.controller;

import com.eduverse.backend.dto.CourseProgressResponseDTO;
import com.eduverse.backend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    // Student checks off a lesson (or checkpoint exercise) as done
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(@PathVariable Long lessonId) {
        progressService.markLessonComplete(lessonId);
        return ResponseEntity.ok().build();
    }

    // Student's completion percentage + whether the course is fully finished
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponseDTO> getProgress(@PathVariable Long courseId) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId));
    }
}
