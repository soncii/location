package com.example.location.repositories;

import com.example.location.entities.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

    CompletableFuture<Optional<User>> findByEmailAndPassword(String email, String password);

    CompletableFuture<Optional<User>> findByEmail(String email);

    CompletableFuture<Optional<User>> findById(Long uid);

    CompletableFuture<User> save(User l);

    CompletableFuture<Boolean> deleteById(Long uid);
}
