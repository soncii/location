package com.example.location.services;

import com.example.location.entities.History;
import com.example.location.repositories.HistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2(topic = "HISTORY")
public class HistoryService {

    public final HistoryRepository historyRepository;

    @EventListener
    public void handleObjectEvent(History history) {

        historyRepository.save(history).thenAccept(x -> {
            if (x.getHid() == null) {
                log.error("Failed to save history event {}", history);
            } else {
                log.info("History event saved {}", history);
            }
        });
    }
}
