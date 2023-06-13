package com.example.location.repositories;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AccessRepositoryImpl implements AccessRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccessRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Access> findAllByLid(Long lid) {
        String sql = "SELECT * FROM access WHERE lid = ?";
        return jdbcTemplate.query(sql, new Object[]{lid}, accessRowMapper);
    }

    @Override
    public List<UserAccessDto> getUserAccessByLocationId(Long lid) {
        String sql = "SELECT a.aid, u.firstname, u.lastname FROM access a " +
                "JOIN users u ON a.uid = u.uid " +
                "WHERE a.lid = ?";
        return jdbcTemplate.query(sql, new Object[]{lid}, userDtoRowMapper);
    }

    @Override
    public Integer deleteByUidAndLid(Long uid, Long lid) {
        String sql = "DELETE FROM access WHERE uid = ? AND lid = ?";
        return jdbcTemplate.update(sql, uid, lid);
    }

    @Override
    public Optional<Access> findByUidAndLid(Long uid, Long lid) {
        String sql = "SELECT * FROM access WHERE uid = ? AND lid = ?";
        List<Access> accesses = jdbcTemplate.query(sql, new Object[]{uid, lid}, accessRowMapper);
        return accesses.isEmpty() ? Optional.empty() : Optional.of(accesses.get(0));
    }

    @Override
    public Access save(Access a) {
        String sql = "INSERT INTO access (uid, lid, type) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, a.getUid(), a.getLid(), a.getType());
        return a;
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
