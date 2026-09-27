package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.Enrollment;
import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.EnrollmentRequestDTO;
import com.eduverse.backend.dto.EnrollmentResponseDTO;
import com.eduverse.backend.exception.CapacityExceededException;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.exception.DuplicateResourceException;
import com.eduverse.backend.exception.ResourceNotFoundException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.EnrollmentRepository;
import com.eduverse.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // 1. Enroll a student using simple object assignments (admin force-assign path)
    public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO dto) {
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        Enrollment saved = createEnrollment(student, course);

        EnrollmentResponseDTO response = new EnrollmentResponseDTO();
        response.setEnrollmentId(saved.getId());
        response.setStudentId(student.getId());
        response.setStudentName(student.getName());
        response.setCourseId(course.getId());
        response.setCourseTitle(course.getTitle());
        response.setEnrollmentDate(saved.getEnrollmentDate());

        return response;
    }

    // Shared enrollment logic used both by admin force-assign and the post-payment flow.
    // Enforces the "limited-seat enrollment" rule: a course with a capacity can't be
    // over-booked once its enrollment count reaches that capacity.
    public Enrollment createEnrollment(User student, Course course) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        if (course.getCapacity() != null) {
            long currentEnrollments = enrollmentRepository.countByCourseId(course.getId());
            if (currentEnrollments >= course.getCapacity()) {
                throw new CapacityExceededException("This course has reached its enrollment capacity");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("action=enrollment_created studentId={} courseId={}", student.getId(), course.getId());
        return saved;
    }

    // 2. Get logged-in student's courses using a clean for-each loop instead of streams
    public List<EnrollmentResponseDTO> getMyEnrolledCourses() {
        String studentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<EnrollmentResponseDTO> dtos = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            EnrollmentResponseDTO response = new EnrollmentResponseDTO();
            response.setEnrollmentId(enrollment.getId());
            response.setStudentId(student.getId());
            response.setStudentName(student.getName());
            response.setCourseId(enrollment.getCourse().getId());
            response.setCourseTitle(enrollment.getCourse().getTitle());
            response.setEnrollmentDate(enrollment.getEnrollmentDate());

            dtos.add(response);
        }

        return dtos;
    }
}
