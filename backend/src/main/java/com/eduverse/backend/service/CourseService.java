package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.CourseRequestDTO;
import com.eduverse.backend.dto.CourseResponseDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.EnrollmentRepository;
import com.eduverse.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewService reviewService;

    public CourseService(CourseRepository courseRepository,
                          UserRepository userRepository,
                          EnrollmentRepository enrollmentRepository,
                          ReviewService reviewService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reviewService = reviewService;
    }

    // Public catalog - students only ever see published courses.
    // Cached since the catalog is read far more often than it changes.
    @Cacheable("publishedCourses")
    public List<CourseResponseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseResponseDTO> dtos = new ArrayList<>();

        for (Course course : courses) {
            if (course.isPublished()) {
                dtos.add(convertToResponseDTO(course));
            }
        }

        return dtos;
    }

    // Instructor's own dashboard - shows drafts and published courses alike
    public List<CourseResponseDTO> getMyCourses() {
        String instructorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("User not found in database."));

        List<CourseResponseDTO> dtos = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (course.getInstructor() != null && course.getInstructor().getId().equals(instructor.getId())) {
                dtos.add(convertToResponseDTO(course));
            }
        }
        return dtos;
    }

    @Cacheable(value = "courseById", key = "#id")
    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + id + " not found"));

        // Popularity tracking: bump the view counter every time a course is looked up.
        // Not @CacheEvict'd on purpose - a cached response is fine for this metric.
        course.setViewCount(course.getViewCount() + 1);
        courseRepository.save(course);

        return convertToResponseDTO(course);
    }

    @CacheEvict(value = {"publishedCourses", "courseById"}, allEntries = true)
    public CourseResponseDTO addCourse(CourseRequestDTO dto) {
        String instructorEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("User not found in database."));

        // Copy incoming DTO data into a new database Entity object
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setPrice(dto.getPrice());
        course.setDuration(dto.getDuration());
        course.setLevel(dto.getLevel());
        course.setCategory(dto.getCategory());
        course.setCapacity(dto.getCapacity());
        course.setPublished(dto.isPublished());

        // Link the logged-in user as the instructor
        course.setInstructor(instructor);

        // Save to database and turn it into a response DTO
        Course savedCourse = courseRepository.save(course);
        log.info("action=course_created courseId={} instructorId={}", savedCourse.getId(), instructor.getId());
        return convertToResponseDTO(savedCourse);
    }

    @CacheEvict(value = {"publishedCourses", "courseById"}, allEntries = true)
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + id + " not found"));

        // Replace the old values with the new values from the DTO
        existingCourse.setTitle(dto.getTitle());
        existingCourse.setDescription(dto.getDescription());
        existingCourse.setPrice(dto.getPrice());
        existingCourse.setDuration(dto.getDuration());
        existingCourse.setLevel(dto.getLevel());
        existingCourse.setCategory(dto.getCategory());
        existingCourse.setCapacity(dto.getCapacity());
        existingCourse.setPublished(dto.isPublished());

        // Save changes back to the database
        Course updatedCourse = courseRepository.save(existingCourse);
        return convertToResponseDTO(updatedCourse);
    }

    @CacheEvict(value = {"publishedCourses", "courseById"}, allEntries = true)
    public CourseResponseDTO setPublished(Long id, boolean published) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + id + " not found"));
        course.setPublished(published);
        Course saved = courseRepository.save(course);
        log.info("action=course_publish_toggled courseId={} published={}", id, published);
        return convertToResponseDTO(saved);
    }

    @CacheEvict(value = {"publishedCourses", "courseById"}, allEntries = true)
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException("Course with ID " + id + " not found");
        }
        courseRepository.deleteById(id);
        log.info("action=course_deleted courseId={}", id);
    }

    // Helper: Turn a raw database Course entity into a clean response DTO
    private CourseResponseDTO convertToResponseDTO(Course course) {
        CourseResponseDTO response = new CourseResponseDTO();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setPrice(course.getPrice());
        response.setDuration(course.getDuration());
        response.setLevel(course.getLevel());
        response.setCategory(course.getCategory());
        response.setCapacity(course.getCapacity());
        response.setPublished(course.isPublished());
        response.setViewCount(course.getViewCount());

        long enrolledCount = enrollmentRepository.countByCourseId(course.getId());
        response.setEnrolledCount(enrolledCount);
        response.setSeatsAvailable(course.getCapacity() == null
                ? null
                : Math.max(0, course.getCapacity() - (int) enrolledCount));
        response.setAverageRating(reviewService.getAverageRating(course.getId()));

        // Securely check if an instructor exists before fetching their name
        if (course.getInstructor() != null) {
            response.setInstructorName(course.getInstructor().getName());
        }

        return response;
    }
}
