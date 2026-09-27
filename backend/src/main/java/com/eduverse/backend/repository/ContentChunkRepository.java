package com.eduverse.backend.repository;

import com.eduverse.backend.Entity.ContentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentChunkRepository extends JpaRepository<ContentChunk, Long> {
    List<ContentChunk> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
