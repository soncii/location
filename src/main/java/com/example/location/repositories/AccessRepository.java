package com.example.location.repositories;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface AccessRepository extends JpaRepository< Access,Long> {
    List<Access> findAllByLid(Long lid);
    @Query(nativeQuery = true, name = "getUserAccessByLocationId")
    List<UserAccessDto> getUserAccessByLocationId(Long lid);
    Integer deleteByUidAndLid(Long uid, Long lid);
    Optional<Access> findByUidAndLid(Long uid,Long lid);

}
