package com.eduverse.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ModuleResponseDTO {
    private Long id;
    private String title;
    private int sequence;
    private Long courseId;
    private List<LessonResponseDTO> lessons;
}
