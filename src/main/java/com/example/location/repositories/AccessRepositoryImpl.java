package com.example.location.repositories;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
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
public class AccessRepositoryImpl implements AccessRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccessRepositoryImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CompletableFuture<List<Access>> findAllByLid(Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM access WHERE lid = ?";
            return jdbcTemplate.query(sql, accessRowMapper, lid);
        });
    }

    @Override
    public CompletableFuture<List<UserAccessDto>> getUserAccessByLocationId(Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT a.aid, u.firstname, u.lastname, a.type, u.email FROM access a " + "JOIN users u ON a" +
                ".uid = u.uid " + "WHERE a.lid = ?";
            return jdbcTemplate.query(sql, userDtoRowMapper, lid);
        });
    }

    @Override
    public CompletableFuture<Integer> deleteByUidAndLid(Long uid, Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM access WHERE uid = ? AND lid = ?";
            return jdbcTemplate.update(sql, uid, lid);
        });
    }

    @Override
    public CompletableFuture<Optional<Access>> findByUidAndLid(Long uid, Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM access WHERE uid = ? AND lid = ?";
            List<Access> accesses = jdbcTemplate.query(sql, accessRowMapper, uid, lid);
            return accesses.isEmpty() ? Optional.empty() : Optional.of(accesses.get(0));
        });
    }

    @Override
    public CompletableFuture<Access> save(Access a) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO access (uid, lid, type) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, a.getUid());
                ps.setLong(2, a.getLid());
                ps.setString(3, a.getType());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                long generatedKey = keyHolder.getKey().longValue();
                a.setAid(generatedKey);
            }

            return a;
        });
    }

    @Override
    public CompletableFuture<Boolean> update(Access a) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE access a set a.type=?";
            int update = jdbcTemplate.update(sql, a.getType());
            return update != 0;
        });
    }

    private final RowMapper<Access> accessRowMapper = (rs, rowNum) -> {
        Access access = new Access();
        access.setAid(rs.getLong("aid"));
        access.setUid(rs.getLong("uid"));
        access.setLid(rs.getLong("lid"));
        access.setType(rs.getString("type"));
        return access;
    };

    private final RowMapper<UserAccessDto> userDtoRowMapper = (rs, rowNum) -> {
        UserAccessDto dto = new UserAccessDto();
        dto.setAccessType(rs.getString("type"));
        dto.setEmail(rs.getString("email"));
        dto.setFirstName(rs.getString("firstname"));
        dto.setLastName(rs.getString("lastname"));
        return dto;
    };
}
