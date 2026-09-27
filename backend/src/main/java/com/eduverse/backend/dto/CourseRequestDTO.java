package com.eduverse.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // Generates the empty constructor
@AllArgsConstructor // Generates the parameterized constructor
public class CourseRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Price is required") // Added to prevent null numbers
    @Min(value = 0, message = "Price cannot be negative")
    private Double price; // Changed to Double wrapper

    @NotBlank(message = "Duration is required")
    private String duration;

    @NotBlank(message = "Level is required")
    private String level;

    @NotBlank(message = "Category is required")
    private String category;

    // Optional - null/omitted means unlimited seats
    @Min(value = 1, message = "Capacity must be at least 1 if provided")
    private Integer capacity;

    // Defaults to false (draft) if omitted - use the /courses/{id}/publish endpoint to flip it
    private boolean published = false;
}
