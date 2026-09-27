package com.eduverse.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private Long courseId;
    private String courseTitle;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
