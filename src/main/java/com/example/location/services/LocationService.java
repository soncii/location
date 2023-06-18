package com.example.location.services;

import com.example.location.dto.LocationDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationService {
    CompletableFuture<List<LocationDTO>> findUserLocations(String uidString);

    CompletableFuture<Location> saveLocation(String uid, String name, String address);

    CompletableFuture<Optional<Location>> findById(Long lid);

    CompletableFuture<List<SharedLocation>> findAllLocations(String uid);
}
