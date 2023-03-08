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
import java.util.List;

@Repository
public interface AccessRepository extends JpaRepository< Access,Long> {
    List<Access> findAllByLid(Long lid);
    @Query(nativeQuery = true, name = "getUserAccessByLocationId")
    List<UserAccessDto> getUserAccessByLocationId(Long lid);

}
