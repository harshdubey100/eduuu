package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModuleRequestDTO {

    @NotBlank(message = "Module title cannot be empty")
    private String title;

    @NotNull(message = "Sequence position is required")
    private Integer sequence;
}
