package com.github.kanttanhed.user_service.repository;

import com.github.kanttanhed.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}