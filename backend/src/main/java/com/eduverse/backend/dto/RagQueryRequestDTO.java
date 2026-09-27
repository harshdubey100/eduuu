package com.eduverse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RagQueryRequestDTO {

    @NotBlank(message = "Question cannot be empty")
    private String question;
}
