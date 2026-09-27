package com.eduverse.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseProgressResponseDTO {
    private Long courseId;
    private String courseTitle;
    private int totalLessons;
    private int completedLessons;
    private double progressPercentage;
    private boolean courseCompleted;
}
