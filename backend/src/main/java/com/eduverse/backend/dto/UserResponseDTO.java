package com.eduverse.backend.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
}
