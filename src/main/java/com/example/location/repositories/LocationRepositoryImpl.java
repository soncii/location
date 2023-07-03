package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class LocationRepositoryImpl implements LocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocationRepositoryImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CompletableFuture<List<Location>> findAllByUid(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM location WHERE uid = ?";
            return jdbcTemplate.query(sql, new LocationRowMapper(), uid);
        });
    }

    @Override
    public CompletableFuture<Optional<Location>> findByUidAndLid(Long uid, Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM location WHERE uid = ? AND lid = ?";
            List<Location> locations = jdbcTemplate.query(sql,new LocationRowMapper(), uid, lid);
            return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
        });
    }

    @Override
    public CompletableFuture<List<SharedLocation>> findAllSharedLocation(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT l.lid, u.email, l.name, l.address, a.type AS accessType " +
                "FROM location l " +
                "JOIN access a ON l.lid = a.lid " +
                "JOIN users u ON u.uid = l.uid " +
                "WHERE a.uid = ?";
            return jdbcTemplate.query(sql, new SharedLocationRowMapper(), uid);
        });
    }

    @Override
    public CompletableFuture<Location> save(Location l) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO location (uid, name, address) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, l.getUid());
                ps.setString(2, l.getName());
                ps.setString(3, l.getAddress());
                return ps;
            }, keyHolder);

            Long lid = keyHolder.getKey().longValue();
            l.setLid(lid);
            return l;
        });
    }

    @Override
    public CompletableFuture<Optional<Location>> findById(Long lid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM location WHERE lid = ?";
            List<Location> locations = jdbcTemplate.query(sql, new LocationRowMapper(), lid);
            return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long lid) {

            return CompletableFuture.supplyAsync(() -> {
                String sql = "DELETE FROM location WHERE lid = ?";
                return jdbcTemplate.update(sql, lid) > 0;
            });
    }

    private static class LocationRowMapper implements RowMapper<Location> {

        @Override
        public Location mapRow(ResultSet rs, int rowNum) throws SQLException {

            Location location = new Location();
            location.setLid(rs.getLong("lid"));
            location.setUid(rs.getLong("uid"));
            location.setName(rs.getString("name"));
            location.setAddress(rs.getString("address"));
            return location;
        }
    }

    private static class SharedLocationRowMapper implements RowMapper<SharedLocation> {

        @Override
        public SharedLocation mapRow(ResultSet rs, int rowNum) throws SQLException {

            SharedLocation sharedLocation = new SharedLocation();
            sharedLocation.setLid(rs.getLong("lid"));
            sharedLocation.setEmail(rs.getString("email"));
            sharedLocation.setName(rs.getString("name"));
            sharedLocation.setAddress(rs.getString("address"));
            sharedLocation.setAccessType(rs.getString("accessType"));
            return sharedLocation;
        }
    }
}
