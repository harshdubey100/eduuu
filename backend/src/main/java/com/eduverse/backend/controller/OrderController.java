package com.eduverse.backend.controller;

import com.eduverse.backend.dto.OrderRequestDTO;
import com.eduverse.backend.dto.OrderResponseDTO;
import com.eduverse.backend.dto.PaymentWebhookRequestDTO;
import com.eduverse.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Student starts a purchase - creates a PENDING order
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = orderService.createOrder(dto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    // 2. Payment gateway calls this once a charge resolves. In production this
    // would sit behind signature verification instead of being open like this demo.
    @PostMapping("/payment-webhook")
    public ResponseEntity<OrderResponseDTO> paymentWebhook(@Valid @RequestBody PaymentWebhookRequestDTO dto) {
        OrderResponseDTO order = orderService.handlePaymentWebhook(dto);
        return ResponseEntity.ok(order);
    }

    // 3. Student's own purchase history
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }
}
