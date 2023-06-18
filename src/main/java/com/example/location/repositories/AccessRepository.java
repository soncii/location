package com.example.location.repositories;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccessRepository {
    List<Access> findAllByLid(Long lid);
    @Query(nativeQuery = true, name = "getUserAccessByLocationId")
    List<UserAccessDto> getUserAccessByLocationId(@Param("lid")Long lid);
    Integer deleteByUidAndLid(Long uid, Long lid);
    Optional<Access> findByUidAndLid(Long uid,Long lid);

    Access save(Access a);

    Boolean update(Access a);
}
