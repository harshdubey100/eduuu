package com.eduverse.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "Email is required for login")
    @Email(message = "Please provide a valid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
