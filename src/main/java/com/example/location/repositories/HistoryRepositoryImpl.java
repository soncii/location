package com.example.location.repositories;

import com.example.location.entities.History;
import com.example.location.entities.Location;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CompletableFuture<History> save(History history) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO history (action_by, object_type, action, action_details, date) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, history.getActionBy());
                ps.setString(2, history.getObjectType());
                ps.setString(3, history.getAction());
                ps.setString(4, history.getActionDetails());
                ps.setTimestamp(5, history.getDate());
                return ps;
            }, keyHolder);
            history.setHid(keyHolder.getKey().longValue());
            return history;
        });
    }
}
