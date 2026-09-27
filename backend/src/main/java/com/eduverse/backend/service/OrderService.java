package com.eduverse.backend.service;

import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.Order;
import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.OrderRequestDTO;
import com.eduverse.backend.dto.OrderResponseDTO;
import com.eduverse.backend.dto.PaymentWebhookRequestDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.exception.ResourceNotFoundException;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.OrderRepository;
import com.eduverse.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Handles course purchasing. In production this would call out to a real
// gateway (Stripe/Razorpay) to create a checkout session; here the "gateway"
// is simulated so the full order -> webhook -> enrollment flow is demoable
// end-to-end without external API keys.
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    public OrderService(OrderRepository orderRepository,
                         UserRepository userRepository,
                         CourseRepository courseRepository,
                         EnrollmentService enrollmentService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
    }

    // 1. Student starts a purchase - creates a PENDING order (like starting a checkout session)
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        User student = getLoggedInStudent();

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + dto.getCourseId() + " not found"));

        Order order = new Order();
        order.setStudent(student);
        order.setCourse(course);
        order.setAmount(course.getPrice());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("action=order_created orderId={} studentId={} courseId={}", saved.getId(), student.getId(), course.getId());

        return convertToResponseDTO(saved);
    }

    // 2. Payment gateway calls this once a charge succeeds or fails.
    // On success, the student is automatically enrolled - this is the
    // "payment completion webhook enabling enrollment" flow from the spec.
    public OrderResponseDTO handlePaymentWebhook(PaymentWebhookRequestDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + dto.getOrderId() + " not found"));

        if ("SUCCESS".equalsIgnoreCase(dto.getStatus())) {
            order.setStatus("PAID");
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);

            enrollmentService.createEnrollment(order.getStudent(), order.getCourse());
            log.info("action=payment_succeeded orderId={} studentId={} courseId={}",
                    order.getId(), order.getStudent().getId(), order.getCourse().getId());
        } else {
            order.setStatus("FAILED");
            orderRepository.save(order);
            log.warn("action=payment_failed orderId={}", order.getId());
        }

        return convertToResponseDTO(order);
    }

    public java.util.List<OrderResponseDTO> getMyOrders() {
        User student = getLoggedInStudent();
        java.util.List<Order> orders = orderRepository.findByStudentId(student.getId());
        java.util.List<OrderResponseDTO> dtos = new java.util.ArrayList<>();

        for (Order order : orders) {
            dtos.add(convertToResponseDTO(order));
        }
        return dtos;
    }

    private User getLoggedInStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getId());
        dto.setCourseId(order.getCourse().getId());
        dto.setCourseTitle(order.getCourse().getTitle());
        dto.setAmount(order.getAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaidAt(order.getPaidAt());
        return dto;
    }
}
