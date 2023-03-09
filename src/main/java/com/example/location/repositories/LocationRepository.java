package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location,Long> {

    List<Location> findAllByUid(Long uid);

    Optional<Location> findByUidAndLid(Long uid, Long lid);
    @Query(name = "AllLocations", nativeQuery = true)
    List<SharedLocation> findAllSharedLocation(Long uid);

}
