package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.Review;
import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.ReviewRequestDTO;
import com.eduverse.backend.dto.ReviewResponseDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.exception.DuplicateResourceException;
import com.eduverse.backend.exception.ResourceNotFoundException;
import com.eduverse.backend.exception.UnauthorizedActionException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.EnrollmentRepository;
import com.eduverse.backend.repository.ReviewRepository;
import com.eduverse.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ReviewService(ReviewRepository reviewRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // Only students who are actually enrolled can leave a course evaluation
    public ReviewResponseDTO addReview(Long courseId, ReviewRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + courseId + " not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new UnauthorizedActionException("You must be enrolled in this course to review it");
        }

        if (reviewRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new DuplicateResourceException("You have already reviewed this course");
        }

        Review review = new Review();
        review.setStudent(student);
        review.setCourse(course);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return convertToResponseDTO(saved);
    }

    public List<ReviewResponseDTO> getReviewsForCourse(Long courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        List<ReviewResponseDTO> dtos = new ArrayList<>();

        for (Review review : reviews) {
            dtos.add(convertToResponseDTO(review));
        }
        return dtos;
    }

    public double getAverageRating(Long courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        return Math.round((total * 100.0 / reviews.size())) / 100.0;
    }

    private ReviewResponseDTO convertToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setCourseId(review.getCourse().getId());
        dto.setStudentName(review.getStudent().getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
