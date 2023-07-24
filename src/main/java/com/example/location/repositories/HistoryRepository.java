package com.example.location.repositories;

import com.example.location.entities.History;

import java.util.concurrent.CompletableFuture;

public interface HistoryRepository {

    CompletableFuture<History> save(History history);
}
