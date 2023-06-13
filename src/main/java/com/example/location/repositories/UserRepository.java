package com.example.location.repositories;

import com.example.location.entities.Location;
import com.example.location.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> findByEmail(String email);
    Optional<User> findById(Long uid);
    User save(User l);
}
