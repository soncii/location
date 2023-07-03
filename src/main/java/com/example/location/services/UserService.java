package com.example.location.services;

import com.example.location.entities.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface UserService {

    CompletableFuture<Optional<User>> authorize(String email, String password);

    CompletableFuture<User> insertUser(User user);

    CompletableFuture<Optional<User>> findUserById(Long uid);

    CompletableFuture<Boolean> authorizeOwner(String uidString, Long lid);

    CompletableFuture<Boolean> deleteUser(Long uid);
}
