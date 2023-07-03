package com.example.location.services;

import com.example.location.entities.Access;
import com.example.location.util.Util;
import com.example.location.dto.LocationDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationRepository locationRepository;
    private final AccessRepository accessRepository;
    private final UserRepository userRepository;

    public LocationServiceImpl(
        LocationRepository locationRepository,
        AccessRepository accessRepository,
        UserRepository userRepository
    ) {
        this.locationRepository = locationRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<List<LocationDTO>> findUserLocations(String uidString) {

        Long uid = Util.saveParseLong(uidString);
        if (uid == null) {
            logger.warn("Invalid user ID: {}", uidString);
            return CompletableFuture.completedFuture(null);
        }

        return locationRepository.findAllByUid(uid)
            .thenApply(locations -> locations.stream()
                .map(l -> accessRepository.findAllByLid(l.getLid())
                    .thenApply(list -> new LocationDTO(l, list)))
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Location> saveLocation(Location location) {

        return userRepository.findById(location.getUid())
            .thenCompose(user -> {
                if (!user.isPresent()) {
                    logger.warn("User not found for ID: {}", location.getUid());
                    return CompletableFuture.completedFuture(null);
                }
                return locationRepository.save(location);
            });
    }

    @Override
    public CompletableFuture<Optional<Location>> findById(Long lid) {

        return locationRepository.findById(lid);
    }

    @Override
    public CompletableFuture<List<SharedLocation>> findAllLocations(String uidStr) {

        Long uid = Util.saveParseLong(uidStr);
        if (uid == null) {
            logger.warn("Invalid user ID: {}", uidStr);
            return CompletableFuture.completedFuture(null);
        }

        Optional<User> user = userRepository.findById(uid).join();
        if (!user.isPresent()) {
            logger.warn("User not found for ID: {}", uid);
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<List<SharedLocation>> allSharedLocations = locationRepository.findAllSharedLocation(uid);
        CompletableFuture<List<Location>> ownerLocations = locationRepository.findAllByUid(uid);

        return allSharedLocations.thenCombine(ownerLocations, (shared, owner) -> {
            List<SharedLocation> modified = owner.stream()
                .map(location -> CompletableFuture.supplyAsync(() -> new SharedLocation(location,
                    user.get().getEmail())))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            shared.addAll(modified);
            return shared;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long lid) {

            return locationRepository.findById(lid)
                .thenCompose(location -> {
                    if (!location.isPresent()) {
                        logger.warn("Location not found for ID: {}", lid);
                        return CompletableFuture.completedFuture(false);
                    }
                    return locationRepository.deleteById(lid);
                });
    }
}
