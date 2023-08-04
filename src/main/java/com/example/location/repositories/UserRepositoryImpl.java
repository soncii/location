package com.example.location.repositories;

import com.example.location.entities.User;
import com.example.location.util.DbException;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUid(rs.getLong("uid"));
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    };

    @Override
    public CompletableFuture<Optional<User>> findByEmailAndPassword(String email, String password) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            List<User> users = jdbcTemplate.query(sql, userRowMapper, email, password);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findByEmail(String email) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM users WHERE email = ?";
            List<User> users = jdbcTemplate.query(sql, userRowMapper, email);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findById(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM users WHERE uid = ?";
            List<User> users = jdbcTemplate.query(sql, userRowMapper, uid);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }

    @Override
    public CompletableFuture<User> save(User user) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO users (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";

            PreparedStatementCreator psc = con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getFirstName());
                ps.setString(2, user.getLastName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPassword());
                return ps;
            };

            jdbcTemplate.execute(psc, (PreparedStatementCallback<Void>) ps -> {
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedKey = rs.getLong(1);
                        user.setUid(generatedKey);
                    }
                } catch (SQLException ex) {
                    throw new DbException();
                }

                return null;
            });

            return user;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM users WHERE uid = ?";
            return jdbcTemplate.update(sql, uid) != 0;
        });
    }
}

