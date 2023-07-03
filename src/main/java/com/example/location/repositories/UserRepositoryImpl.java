package com.example.location.repositories;

import com.example.location.entities.User;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

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
            List<User> users = jdbcTemplate.query(sql,userRowMapper, email);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findById(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM users WHERE uid = ?";
            List<User> users = jdbcTemplate.query(sql,userRowMapper,uid);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }

    @Override
    public CompletableFuture<User> save(User user) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO users (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getFirstName());
                ps.setString(2, user.getLastName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPassword());
                return ps;
            }, keyHolder);
            user.setUid(keyHolder.getKey().longValue());
            return user;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM users WHERE uid = ?";
            return jdbcTemplate.update(sql, uid)!=0;
        });
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUid(rs.getLong("uid"));
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    };
}

