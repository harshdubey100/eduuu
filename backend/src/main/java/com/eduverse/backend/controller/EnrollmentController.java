package com.eduverse.backend.controller;

import com.eduverse.backend.dto.EnrollmentRequestDTO;
import com.eduverse.backend.dto.EnrollmentResponseDTO;
import com.eduverse.backend.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }


    // Admin endpoint to force-assign a course to a student
    @PostMapping("/admin/force-assign")
    public ResponseEntity<EnrollmentResponseDTO> assignCourseManually(@RequestBody EnrollmentRequestDTO dto) {
        EnrollmentResponseDTO response = enrollmentService.enrollStudent(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get the logged-in student's personal enrollment dashboard
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMyDashboard() {
        List<EnrollmentResponseDTO> myDashboard = enrollmentService.getMyEnrolledCourses();
        return ResponseEntity.ok(myDashboard);
    }
}
