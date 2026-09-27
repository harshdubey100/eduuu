package com.eduverse.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRequestDTO {

    @NotBlank(message = "Name field cannot be left blank")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;


    @NotBlank(message = "Role configuration is required")
    @Pattern(
            regexp = "^(STUDENT|INSTRUCTOR)$",
            message = "Role configuration must be either STUDENT or INSTRUCTOR only"
    )
    private String role;
}
