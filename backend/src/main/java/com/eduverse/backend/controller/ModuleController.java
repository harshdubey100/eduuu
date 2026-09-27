package com.eduverse.backend.controller;

import com.eduverse.backend.dto.ModuleRequestDTO;
import com.eduverse.backend.dto.ModuleResponseDTO;
import com.eduverse.backend.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // Instructor/admin builds out the course curriculum module by module
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<ModuleResponseDTO> addModule(@PathVariable Long courseId, @Valid @RequestBody ModuleRequestDTO dto) {
        ModuleResponseDTO saved = moduleService.addModule(courseId, dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Anyone browsing the course (same visibility as course details) can see its curriculum
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleResponseDTO>> getModules(@PathVariable Long courseId) {
        return ResponseEntity.ok(moduleService.getModulesForCourse(courseId));
    }
}
