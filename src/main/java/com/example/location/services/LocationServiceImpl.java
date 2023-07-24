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
    public CompletableFuture<List<LocationDTO>> findUserLocations(String uidString) {

        Long uid = Long.parseLong(uidString);
        return locationRepository.findAllByUid(uid).thenApply(locations -> locations
            .stream()
            .map(l -> accessRepository.findAllByLid(l.getLid()).thenApply(list -> new LocationDTO(l, list)))
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
            return locationRepository.save(location).thenCompose(saved -> {
                if (saved.getUid() == null) {
                    log.error("Location not saved {}", location);
                    throw new DbException();
                }
                historyEventPublisher.publishHistoryCreatedEvent(user.get().getUid(), "LOCATION", saved);
                return CompletableFuture.completedFuture(saved);
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
    public CompletableFuture<List<SharedLocation>> findAllLocations(String uidStr) throws NumberFormatException{


        Long uid = Long.parseLong(uidStr);
        if (uid == null) {
            log.warn("Invalid user ID: {}", uidStr);
            return CompletableFuture.completedFuture(null);
        }
        log.info("Retrieving all locations for UID: {}", uid);
        return userRepository.findById(uid).thenCompose(user -> {
            if (!user.isPresent()) {
                log.warn("User not found for ID: {}", uid);
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture<List<SharedLocation>> allSharedLocations = locationRepository.findAllSharedLocation(uid);
            CompletableFuture<List<Location>> ownerLocations = locationRepository.findAllByUid(uid);

            return allSharedLocations.thenCombine(ownerLocations, (shared, owner) -> {
                List<SharedLocation> modified =
                    owner.stream().map(location -> CompletableFuture.supplyAsync(() -> new SharedLocation(location,
                        user.get().getEmail()))).map(CompletableFuture::join).collect(Collectors.toList());
                shared.addAll(modified);
                return shared;
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long lid) {

        log.info("Deleting location with ID: {}", lid);
        return locationRepository.findById(lid).thenCompose(location -> {
            if (!location.isPresent()) {
                log.warn("Location not found for ID: {}", lid);
                throw new NotFoundException("Location");
            }
            return locationRepository.deleteById(lid).thenCompose(deleted -> {
                if (Boolean.FALSE.equals(deleted)) {
                    log.error("Location not deleted for ID: {}", lid);
                    throw new DbException();
                }
                historyEventPublisher.publishHistoryDeletedEvent(location.get().getUid(), "LOCATION", location.get());
                return CompletableFuture.completedFuture(Boolean.TRUE);
            });
        });
    }
}
