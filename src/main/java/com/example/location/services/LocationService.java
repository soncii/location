package com.example.location.services;

import com.example.location.dto.LocationDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<List<LocationDTO>> findUserLocations(Long uid);

    CompletableFuture<Location> saveLocation(Location location);

    CompletableFuture<Optional<Location>> findById(Long lid);

    CompletableFuture<List<SharedLocation>> findAllLocations(Long uid);

    CompletableFuture<Boolean> deleteById(Long lid);
}
