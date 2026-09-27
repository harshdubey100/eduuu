package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.Module;
import com.eduverse.backend.dto.LessonResponseDTO;
import com.eduverse.backend.dto.ModuleRequestDTO;
import com.eduverse.backend.dto.ModuleResponseDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.exception.ResourceNotFoundException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
    }

    public ModuleResponseDTO addModule(Long courseId, ModuleRequestDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + courseId + " not found"));

        Module module = new Module();
        module.setTitle(dto.getTitle());
        module.setSequence(dto.getSequence());
        module.setCourse(course);

        Module saved = moduleRepository.save(module);
        return convertToResponseDTO(saved);
    }

    public List<ModuleResponseDTO> getModulesForCourse(Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderBySequenceAsc(courseId);
        List<ModuleResponseDTO> dtos = new ArrayList<>();

        for (Module module : modules) {
            dtos.add(convertToResponseDTO(module));
        }
        return dtos;
    }

    public Module getModuleEntity(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module with ID " + moduleId + " not found"));
    }

    private ModuleResponseDTO convertToResponseDTO(Module module) {
        ModuleResponseDTO dto = new ModuleResponseDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setSequence(module.getSequence());
        dto.setCourseId(module.getCourse().getId());

        List<LessonResponseDTO> lessonDtos = new ArrayList<>();
        if (module.getLessons() != null) {
            for (var lesson : module.getLessons()) {
                LessonResponseDTO lessonDto = new LessonResponseDTO();
                lessonDto.setId(lesson.getId());
                lessonDto.setTitle(lesson.getTitle());
                lessonDto.setContent(lesson.getContent());
                lessonDto.setSequence(lesson.getSequence());
                lessonDto.setCheckpoint(lesson.isCheckpoint());
                lessonDto.setDueDate(lesson.getDueDate());
                lessonDto.setModuleId(module.getId());
                lessonDtos.add(lessonDto);
            }
        }
        dto.setLessons(lessonDtos);
        return dto;
    }
}
