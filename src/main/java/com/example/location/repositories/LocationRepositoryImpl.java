package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import com.example.location.dto.UserAccessDto;
import com.example.location.repositories.LocationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationRepositoryImpl implements LocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocationRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Location> findAllByUid(Long uid) {
        String sql = "SELECT * FROM location WHERE uid = ?";
        return jdbcTemplate.query(sql, new Object[]{uid}, new LocationRowMapper());
    }

    @Override
    public Optional<Location> findByUidAndLid(Long uid, Long lid) {
        String sql = "SELECT * FROM location WHERE uid = ? AND lid = ?";
        List<Location> locations = jdbcTemplate.query(sql, new Object[]{uid, lid}, new LocationRowMapper());
        return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
    }

    @Override
    public List<SharedLocation> findAllSharedLocation(Long uid) {
        String sql = "SELECT l.lid, u.email, l.name, l.address, a.type AS accessType " +
                "FROM location l " +
                "JOIN access a ON l.lid = a.lid " +
                "JOIN users u ON u.uid = l.uid " +
                "WHERE a.uid = ?";
        return jdbcTemplate.query(sql, new Object[]{uid}, new SharedLocationRowMapper());
    }

    @Override
    public Location save(Location l) {
        String sql = "INSERT INTO location (uid, name, address) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, l.getUid(), l.getName(), l.getAddress());
        return l;
    }

    @Override
    public Optional<Location> findById(Long uid) {
        String sql = "SELECT * FROM location WHERE lid = ?";
        List<Location> locations = jdbcTemplate.query(sql, new Object[]{uid}, new LocationRowMapper());
        return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
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
