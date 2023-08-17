package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
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
public class LocationRepositoryImpl implements LocationRepository {

    private final JdbcTemplate jdbcTemplate;

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
            List<Location> locations = jdbcTemplate.query(sql, new LocationRowMapper(), uid, lid);
            return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
        });
    }

    @Override
    public CompletableFuture<List<SharedLocation>> findAllLocations(Long uid) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "select l.lid, u.email, l.name, l.address, 'owner' as accessType\n" +
                "from location l\n" +
                "         inner join users u on u.uid = l.uid\n" +
                "where l.uid = ?\n" +
                "UNION\n" +
                "select l.lid, u.email, l.name, l.address, a.type\n" +
                "from location l\n" +
                "    inner join access a on l.lid = a.lid\n" +
                "    inner join users u on a.uid = u.uid\n" +
                "where l.lid in (select lid from access where uid = ?);\n";
            return jdbcTemplate.query(sql, new SharedLocationRowMapper(), uid, uid);
        });
    }

    @Override
    public CompletableFuture<Location> save(Location l) {

        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO location (uid, name, address) VALUES (?, ?, ?)";

            // Prepare the PreparedStatementCreator with RETURN_GENERATED_KEYS option
            PreparedStatementCreator psc = con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, l.getUid());
                ps.setString(2, l.getName());
                ps.setString(3, l.getAddress());
                return ps;
            };

            // Execute the update with PreparedStatementCallback to retrieve the generated keys
            jdbcTemplate.execute(psc, (PreparedStatementCallback<Void>) ps -> {
                ps.executeUpdate();

                // Retrieve the generated keys from the PreparedStatement
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedKey = rs.getLong(1);
                        l.setLid(generatedKey);
                    }
                } catch (SQLException ex) {
                    throw new DbException("Could not save location");
                }

                return null;
            });

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
