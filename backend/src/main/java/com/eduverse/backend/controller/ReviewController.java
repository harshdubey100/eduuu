package com.eduverse.backend.controller;

import com.eduverse.backend.dto.ReviewRequestDTO;
import com.eduverse.backend.dto.ReviewResponseDTO;
import com.eduverse.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses/{courseId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Enrolled students leave a rating + comment (course evaluation)
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(@PathVariable Long courseId, @Valid @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO saved = reviewService.addReview(courseId, dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getReviewsForCourse(courseId));
    }
}
