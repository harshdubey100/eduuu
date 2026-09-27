package com.eduverse.backend.repository;

import com.eduverse.backend.Entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseIdOrderBySequenceAsc(Long courseId);
}
