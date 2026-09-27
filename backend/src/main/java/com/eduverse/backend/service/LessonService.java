package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Lesson;
import com.eduverse.backend.Entity.Module;
import com.eduverse.backend.dto.LessonRequestDTO;
import com.eduverse.backend.dto.LessonResponseDTO;
import com.eduverse.backend.repository.LessonRepository;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleService moduleService;
    private final RagService ragService;

    public LessonService(LessonRepository lessonRepository, ModuleService moduleService, RagService ragService) {
        this.lessonRepository = lessonRepository;
        this.moduleService = moduleService;
        this.ragService = ragService;
    }

    public LessonResponseDTO addLesson(Long moduleId, LessonRequestDTO dto) {
        Module module = moduleService.getModuleEntity(moduleId);

        Lesson lesson = new Lesson();
        lesson.setTitle(dto.getTitle());
        lesson.setContent(dto.getContent());
        lesson.setSequence(dto.getSequence());
        lesson.setCheckpoint(dto.isCheckpoint());
        lesson.setDueDate(dto.getDueDate());
        lesson.setModule(module);

        Lesson saved = lessonRepository.save(lesson);

        // Re-chunk the parent course's content so the new lesson is searchable via RAG
        ragService.reindexCourse(module.getCourse().getId());

        return convertToResponseDTO(saved);
    }

    private LessonResponseDTO convertToResponseDTO(Lesson lesson) {
        LessonResponseDTO dto = new LessonResponseDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContent(lesson.getContent());
        dto.setSequence(lesson.getSequence());
        dto.setCheckpoint(lesson.isCheckpoint());
        dto.setDueDate(lesson.getDueDate());
        dto.setModuleId(lesson.getModule().getId());
        return dto;
    }
}
