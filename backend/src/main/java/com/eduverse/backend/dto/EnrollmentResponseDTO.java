package com.eduverse.backend.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class EnrollmentResponseDTO {
    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime enrollmentDate;
}
