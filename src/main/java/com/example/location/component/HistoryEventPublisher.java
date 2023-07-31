package com.example.location.component;

import com.example.location.entities.History;
import com.example.location.util.Util;
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
    public <T> void publishHistoryCreatedEvent(Long actionBy, Util.ObjectType objectType, T object) {

        History event = new History(null, actionBy, objectType.name(), Util.ActionType.CREATED.name(), objectMapper.writeValueAsString(object),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }

    @SneakyThrows
    public <T> void publishHistoryDeletedEvent(Long actionBy, Util.ObjectType objectType, T object) {

        History event = new History(null, actionBy, objectType.name(), Util.ActionType.DELETED.name(), objectMapper.writeValueAsString(object),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }


    @SneakyThrows
    public <T> void publishHistoryUpdatedEvent(Long actionBy, Util.ObjectType objectType, T oldObject, T newObject) {

        History event = new History(null, actionBy, objectType.name(), Util.ActionType.UPDATED.name(),
            objectMapper.writeValueAsString(oldObject) +" -> " +objectMapper.writeValueAsString(newObject),
            new Timestamp(System.currentTimeMillis()));
        eventPublisher.publishEvent(event);
    }
}

