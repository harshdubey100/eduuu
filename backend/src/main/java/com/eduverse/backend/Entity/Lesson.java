package com.eduverse.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // The actual learning content/body text for this lesson (also chunked for RAG search)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // Display order of this lesson within its module
    @Column(nullable = false)
    private int sequence;

    // Marks a lesson as a graded checkpoint exercise rather than plain reading material
    @Column(nullable = false)
    private boolean checkpoint = false;

    // Optional soft deadline for completing this lesson/assessment
    private LocalDateTime dueDate;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
}
