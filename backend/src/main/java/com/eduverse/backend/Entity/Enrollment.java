package com.eduverse.backend.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Connects directly to the User table (The Student buying the course)
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Connects directly to the Course table (The Course being bought)
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;
}
