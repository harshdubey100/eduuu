package com.eduverse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Double price;
    private String duration;
    private String level;
    private String category;

    // ✅ ADDED: Displays the teacher's name dynamically to the frontend developer!
    private String instructorName;

    private Integer capacity;
    private boolean published;
    private long viewCount;
    private long enrolledCount;
    private Integer seatsAvailable; // null when capacity is unlimited
    private double averageRating;
}
