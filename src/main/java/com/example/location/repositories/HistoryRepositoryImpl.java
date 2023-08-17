package com.example.location.repositories;

import com.example.location.entities.History;
import com.example.location.util.DbException;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CompletableFuture<History> save(History history) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO history (action_by, object_type, action, action_details, date) VALUES (?, ?, ?,"
                + " ?, ?)";

            PreparedStatementCreator psc = con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, history.getActionBy());
                ps.setString(2, history.getObjectType());
                ps.setString(3, history.getAction());
                ps.setString(4, history.getActionDetails());
                ps.setTimestamp(5, history.getDate());
                return ps;
            };

            jdbcTemplate.execute(psc, (PreparedStatementCallback<Void>) ps -> {
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedKey = rs.getLong(1);
                        history.setHid(generatedKey);
                    }
                } catch (SQLException ex) {
                    throw new DbException("Could not save history");
                }

                return null;
            });

            return history;
        });
    }
}
