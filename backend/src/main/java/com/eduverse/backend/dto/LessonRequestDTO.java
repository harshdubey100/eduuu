package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LessonRequestDTO {

    @NotBlank(message = "Lesson title cannot be empty")
    private String title;

    @NotBlank(message = "Lesson content cannot be empty")
    private String content;

    @NotNull(message = "Sequence position is required")
    private Integer sequence;

    private boolean checkpoint = false;

    private LocalDateTime dueDate;
}
