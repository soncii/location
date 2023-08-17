package com.example.location.services;

import com.example.location.component.HistoryEventPublisher;
import com.example.location.dto.LocationDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import com.example.location.util.DbException;
import com.example.location.util.NotFoundException;
import com.example.location.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final AccessRepository accessRepository;
    private final UserRepository userRepository;

    private final HistoryEventPublisher historyEventPublisher;

    @Override
    public CompletableFuture<List<LocationDTO>> findUserLocations(Long uid) {

        return locationRepository.findAllByUid(uid).thenApply(locations -> locations
            .stream()
            .map(location -> accessRepository.findAllByLid(location.getLid()).thenApply(list -> new LocationDTO(location, list)))
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Location> saveLocation(Location location) {

        log.info("Saving location: {}", location);
        return userRepository.findById(location.getUid()).thenCompose(user -> {
            if (!user.isPresent()) {
                log.warn("User not found for ID: {}", location.getUid());
                throw new NotFoundException("User");
            }
            return locationRepository.save(location).thenApply(saved -> {
                if (saved.getUid() == null) {
                    log.error("Location not saved {}", location);
                    throw new DbException("Could not save location");
                }
                historyEventPublisher.publishHistoryCreatedEvent(user.get().getUid(), Util.ObjectType.LOCATION, saved);
                return saved;
            });
        });
    }

    @Override
    public CompletableFuture<Optional<Location>> findById(Long lid) {

        log.info("Retrieving location with ID: {}", lid);
        return locationRepository.findById(lid).thenApply(location -> {
            if (!location.isPresent()) {
                log.warn("Location not found for ID: {}", lid);
                throw new NotFoundException("Location");
            }
            return location;
        });
    }

    @Override
    public CompletableFuture<List<SharedLocation>> findAllLocations(Long uid) throws NumberFormatException {

        log.info("Retrieving all locations for UID: {}", uid);
        return locationRepository.findAllLocations(uid);
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long lid) {

        log.info("Deleting location with ID: {}", lid);
        return locationRepository.findById(lid).thenCompose(location -> {
            if (!location.isPresent()) {
                log.warn("Location not found for ID: {}", lid);
                throw new NotFoundException("Location");
            }
            return locationRepository.deleteById(lid).thenApply(isDeleted -> {
                if (!isDeleted) {
                    log.error("Location not deleted for ID: {}", lid);
                    throw new DbException("Could not delete location");
                }
                historyEventPublisher.publishHistoryDeletedEvent(location.get().getUid(), Util.ObjectType.LOCATION,
                    location.get());
                return isDeleted;
            });
        });
    }
}
