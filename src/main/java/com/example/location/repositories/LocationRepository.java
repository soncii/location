package com.example.location.repositories;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationRepository {

    CompletableFuture<List<Location>> findAllByUid(Long uid);

    CompletableFuture<Optional<Location>> findByUidAndLid(Long uid, Long lid);

    CompletableFuture<List<SharedLocation>> findAllSharedLocation(@Param("uid") Long uid);

    CompletableFuture<Location> save(Location l);

    CompletableFuture<Optional<Location>> findById(Long uid);
}
