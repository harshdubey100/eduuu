package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequestDTO {

    @NotNull(message = "Course reference ID is required")
    private Long courseId;
}
