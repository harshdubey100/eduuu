package com.eduverse.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Tracks whether a given student has completed a given lesson
@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "lesson_id"}))
@Getter
@Setter
@NoArgsConstructor
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedAt;
}
