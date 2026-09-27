package com.eduverse.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long courseId;
    private String studentName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
