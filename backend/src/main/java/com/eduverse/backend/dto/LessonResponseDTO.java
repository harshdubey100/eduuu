package com.eduverse.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LessonResponseDTO {
    private Long id;
    private String title;
    private String content;
    private int sequence;
    private boolean checkpoint;
    private LocalDateTime dueDate;
    private Long moduleId;
}
