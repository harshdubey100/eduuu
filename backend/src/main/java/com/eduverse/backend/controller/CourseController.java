package com.eduverse.backend.controller;

import com.eduverse.backend.dto.CourseRequestDTO;
import com.eduverse.backend.dto.CourseResponseDTO;
import com.eduverse.backend.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // 1. Get a list of all available courses
    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {
        List<CourseResponseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // 1b. Instructor's own dashboard, including unpublished drafts
    @GetMapping("/my")
    public ResponseEntity<List<CourseResponseDTO>> getMyCourses() {
        return ResponseEntity.ok(courseService.getMyCourses());
    }

    // 2. Fetch details for a specific course by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourseById(@PathVariable long id) {
        CourseResponseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // 3. Create and publish a new course
    @PostMapping
    public ResponseEntity<CourseResponseDTO> addCourse(@Valid @RequestBody CourseRequestDTO dto) {
        CourseResponseDTO savedCourse = courseService.addCourse(dto);
        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    // 4. Update an existing course by its ID
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequestDTO dto) {
        CourseResponseDTO updatedCourse = courseService.updateCourse(id, dto);
        return ResponseEntity.ok(updatedCourse);
    }

    // 5. Toggle whether a course is publicly visible/enrollable
    @PatchMapping("/{id}/publish")
    public ResponseEntity<CourseResponseDTO> setPublished(@PathVariable Long id, @RequestParam boolean published) {
        CourseResponseDTO updated = courseService.setPublished(id, published);
        return ResponseEntity.ok(updated);
    }

    // 6. Remove a course entirely
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
