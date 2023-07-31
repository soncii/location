package com.example.location.services;

import com.example.location.entities.History;
import com.example.location.repositories.HistoryRepository;
import com.example.location.util.DbException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Log4j2
public class HistoryService {

    private final HistoryRepository historyRepository;

    @EventListener
    public CompletableFuture<Void> handleObjectEvent(History history) {

        return historyRepository.save(history).thenAccept(x -> {
            if (x.getHid() == null) {
                log.error("Failed to save history event {}", history);
            } else {
                log.info("History event saved {}", history);
            }
        });
    }
}
