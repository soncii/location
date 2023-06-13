package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {

    List<Location> findAllByUid(Long uid);

    Optional<Location> findByUidAndLid(Long uid, Long lid);
    @Query(name = "AllLocations", nativeQuery = true)
    List<SharedLocation> findAllSharedLocation(@Param("uid")Long uid);
    Location save(Location l);
    Optional<Location> findById(Long uid);
}
