package com.eduverse.backend.repository;


import com.eduverse.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface  UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
