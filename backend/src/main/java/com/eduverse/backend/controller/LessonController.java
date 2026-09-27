package com.eduverse.backend.controller;

import com.eduverse.backend.dto.LessonRequestDTO;
import com.eduverse.backend.dto.LessonResponseDTO;
import com.eduverse.backend.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    // Instructor/admin adds a lesson to a module. Content is automatically
    // chunked and (re)indexed for the course's RAG search right after saving.
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonResponseDTO> addLesson(@PathVariable Long moduleId, @Valid @RequestBody LessonRequestDTO dto) {
        LessonResponseDTO saved = lessonService.addLesson(moduleId, dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
