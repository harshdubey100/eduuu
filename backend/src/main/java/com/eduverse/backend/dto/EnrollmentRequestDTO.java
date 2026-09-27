package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EnrollmentRequestDTO {

    @NotNull(message = "Student reference ID cannot be null")
    private Long studentId;

    @NotNull(message = "Target Course reference ID cannot be null")
    private Long courseId;
}
