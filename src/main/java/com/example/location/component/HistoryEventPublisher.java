package com.example.location.component;

import com.example.location.entities.History;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@AllArgsConstructor
@Component
public class HistoryEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public <T> void publishHistoryCreatedEvent(Long actionBy, String objectType, T object) {

        History event = new History(null, actionBy, objectType, "created", objectMapper.writeValueAsString(object),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }

    @SneakyThrows
    public <T> void publishHistoryDeletedEvent(Long actionBy, String objectType, T object) {

        History event = new History(null, actionBy, objectType, "deleted", objectMapper.writeValueAsString(object),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }


    @SneakyThrows
    public <T> void publishHistoryUpdatedEvent(Long actionBy, String objectType,T oldObject, T newObject) {

        History event = new History(null, actionBy, objectType, "updated",
            objectMapper.writeValueAsString(oldObject) +" -> " +objectMapper.writeValueAsString(newObject),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }
}

