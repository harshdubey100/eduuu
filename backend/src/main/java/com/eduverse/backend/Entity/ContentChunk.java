package com.eduverse.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A retrievable slice of a course's lesson content, stored alongside a
// numeric vector so it can be ranked for relevance against a search query.
// This is the "retrieval" half of the course RAG pipeline.
@Entity
@Table(name = "content_chunks")
@Getter
@Setter
@NoArgsConstructor
public class ContentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String chunkText;

    // Vector stored as a comma-separated list of doubles, one weight per vocabulary term
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String vector;
}
