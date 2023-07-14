package com.example.location.controllers;

import com.example.location.dto.AccessDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.dto.UserLocationDTO;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import com.example.location.util.BadRequestException;
import com.example.location.util.DbException;
import com.example.location.util.ForbidException;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@Log4j2
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    private final AccessService accessService;

    private final UserService userService;

    private static final String EMPTY = "empty";

    @GetMapping("/{lid}")
    public CompletableFuture<ResponseEntity<Location>> getLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        if (uid.equals(EMPTY)) {
            log.warn("Invalid or empty UID cookie received");
            throw new BadRequestException();
        }

        log.info("Retrieving location with ID: {}", lid);
        return userService.authorizeOwner(uid, lid).thenCompose(auth -> {
            if (Boolean.FALSE.equals(auth)) {
                log.warn("User is not authorized to access the location");
                throw new ForbidException();
            }
            return locationService.findById(lid).thenApply(location -> {
                if (!location.isPresent()) {
                    log.error("Location not found");
                    throw new NoSuchElementException("Location Not Found");
                }

                log.info("Retrieved location successfully");
                return ResponseEntity.ok(location.get());
            });
        });



    }

    @PostMapping("")
    public CompletableFuture<ResponseEntity<Location>> saveLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestBody Location location
    ) {

        if (uid.equals(EMPTY)) {
            log.warn("Invalid or empty UID cookie received");
            throw new BadRequestException();
        }
        location.setUid(Long.parseLong(uid));
        log.info("Saving location: {}", location);
        return locationService.saveLocation(location).thenApply(saved -> {
            if (saved.getLid() == null) {
                log.error("Failed to insert location to database");
                throw new DbException();
            }

            log.info("Location saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(location);
        });
    }

    @DeleteMapping("/{lid}")
    public CompletableFuture<ResponseEntity> deleteLocation(@PathVariable("lid") Long lid) {

        log.info("Deleting location with ID: {}", lid);
        return locationService.deleteById(lid).thenApply(deleted -> {
            if (Boolean.FALSE.equals(deleted)) {
                log.error("Failed to delete location");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            log.info("Location deleted successfully");
            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        if (uid.equals(EMPTY)) {
            log.warn("Invalid or empty UID received");
            throw new BadRequestException();
        }

        log.info("Retrieving all locations for UID: {}", uid);
        return locationService.findAllLocations(uid).thenApply(locations -> {
            log.info("Retrieved all locations successfully");
            log.info("Locations: {}", locations);
            return ResponseEntity.ok(locations);
        });
    }

    @PostMapping( "/share")
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestBody AccessDTO access
    ) {

        accessService.validateShareMode(access.getShareMode());
        log.info("Saving share for location with ID: {}", access);
        return accessService.saveAccess(access).thenApply(saved -> {
            if (saved.getAid() == null) {
                log.error("Failed to insert access to database");
                throw new DbException();
            }

            log.info("Share saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        });
    }

    @PostMapping( "/unfriend")
    public CompletableFuture<ResponseEntity<Void>> unfriend(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid()).thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) {
                log.warn("User is not authorized to unfriend from the location");
                throw new ForbidException();
            }

            return accessService.delete(userLocation.getLid(), userLocation.getEmail()).thenApply(result -> {
                if (result) {
                    log.info("User successfully unfriended from the location");
                    return ResponseEntity.ok().build();
                } else {
                    log.error("Failed to unfriend user from the location");
                    throw new DbException();
                }
            });
        });
    }

    @PostMapping("/access")
    public CompletableFuture<ResponseEntity<Void>> changeMode(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid()).thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) {
                log.warn("User is not authorized to change access mode for the location");
                throw new ForbidException();
            }

            return accessService.change(userLocation.getLid(), userLocation.getEmail()).thenApply(result -> {
                if (result) {
                    log.info("Access mode changed successfully");
                    return ResponseEntity.ok().build();
                } else {
                    log.error("Failed to change access mode");
                    return ResponseEntity.internalServerError().build();
                }
            });
        });
    }
}
