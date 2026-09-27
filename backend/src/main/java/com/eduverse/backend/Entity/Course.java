package com.eduverse.backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String duration;
    private String level;
    private String category;
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    // Null capacity means unlimited seats
    private Integer capacity;

    // Draft courses are only visible to their instructor/admins until published
    @Column(nullable = false)
    private boolean published = false;

    // Popularity tracking - bumped every time the course detail page is fetched
    @Column(nullable = false)
    private long viewCount = 0L;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules = new ArrayList<>();
}
