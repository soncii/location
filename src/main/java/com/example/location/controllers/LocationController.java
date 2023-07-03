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
import com.example.location.util.DbSaveException;
import com.example.location.util.ForbidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/location")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    final LocationService locationService;

    final AccessService accessService;

    final UserService userService;

    private static final String ACCESS_DENIED = "Access Denied";

    private static final String EMPTY = "empty";
    private static final String HEADER_NEEDED = "Authorization token is needed";

    @GetMapping(value = "/{lid}")
    public CompletableFuture<ResponseEntity<Location>> getLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) throws ForbidException, NoSuchElementException {

        if (uid.equals(EMPTY)) {
            logger.warn("Invalid or empty UID cookie received");
            throw new BadRequestException(HEADER_NEEDED);
        }

        logger.info("Retrieving location with ID: {}", lid);
        Boolean authorized = userService.authorizeOwner(uid, lid).join();
        if (Boolean.FALSE.equals(authorized)) {
            logger.warn("User is not authorized to access the location");
            throw new ForbidException(ACCESS_DENIED);
        }

        return locationService.findById(lid)
            .thenApply(location -> {
                if (!location.isPresent()) {
                    logger.error("Location not found");
                    throw new NoSuchElementException("Location Not Found");
                }

                logger.info("Retrieved location successfully");
                return ResponseEntity.ok(location.get());
            });
    }

    @PostMapping(value = "")
    public CompletableFuture<ResponseEntity<Location>> saveLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestBody Location location
    ) throws DbSaveException {

        if (uid.equals(EMPTY)) {
            logger.warn("Invalid or empty UID cookie received");
            throw new BadRequestException(HEADER_NEEDED);
        }
        location.setUid(Long.parseLong(uid));
        logger.info("Saving location: {}", location);
        return locationService.saveLocation(location)
            .thenApply(saved -> {
                if (saved.getLid() == null) {
                    logger.error("Failed to insert location to database");
                    throw new DbSaveException("Couldn't insert location to db");
                }

                logger.info("Location saved successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(location);
            });
    }

    @DeleteMapping(value = "/{lid}")
    public CompletableFuture<ResponseEntity> deleteLocation(@PathVariable("lid") Long lid) {

        logger.info("Deleting location with ID: {}", lid);
        return locationService.deleteById(lid)
            .thenApply(deleted -> {
                if (Boolean.FALSE.equals(deleted)) {
                    logger.error("Failed to delete location");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

                logger.info("Location deleted successfully");
                return ResponseEntity.ok().build();
            });
    }

    @GetMapping(value = "/all")
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        if (uid.equals(EMPTY)) {
            logger.warn("Invalid or empty UID received");
            throw new BadRequestException(HEADER_NEEDED);
        }

        logger.info("Retrieving all locations for UID: {}", uid);
        return locationService.findAllLocations(uid)
            .thenApply(locations -> {
                logger.info("Retrieved all locations successfully");
                logger.info("Locations: {}", locations);
                return ResponseEntity.ok(locations);
            });
    }

    @PostMapping(value = "/share")
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestBody AccessDTO access
    ) throws DbSaveException {

        accessService.validateShareMode(access.getShareMode());
        logger.info("Saving share for location with ID: {}", access);
        return accessService.saveAccess(access)
            .thenApply(saved -> {
                if (saved.getAid() == null) {
                    logger.error("Failed to insert access to database");
                    throw new DbSaveException("Couldn't insert access to db");
                }

                logger.info("Share saved successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            });
    }

    @PostMapping(value = "/unfriend")
    public CompletableFuture<ResponseEntity<Void>> unfriend(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) throws ForbidException {

        return userService.authorizeOwner(uid, userLocation.getLid())
            .thenCompose(authorized -> {
                if (Boolean.FALSE.equals(authorized)) {
                    logger.warn("User is not authorized to unfriend from the location");
                    throw new ForbidException(ACCESS_DENIED);
                }

                return accessService.delete(userLocation.getLid(), userLocation.getEmail())
                    .thenApply(result -> {
                        if (result) {
                            logger.info("User successfully unfriended from the location");
                            return ResponseEntity.ok().build();
                        } else {
                            logger.error("Failed to unfriend user from the location");
                            throw new RuntimeException();
                        }
                    });
            });
    }

    @PostMapping(value = "/access", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Void>> changeMode(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid())
            .thenCompose(authorized -> {
                if (Boolean.FALSE.equals(authorized)) {
                    logger.warn("User is not authorized to change access mode for the location");
                    throw new ForbidException(ACCESS_DENIED);
                }

                return accessService.change(userLocation.getLid(), userLocation.getEmail())
                    .thenApply(result -> {
                        if (result) {
                            logger.info("Access mode changed successfully");
                            return ResponseEntity.ok().build();
                        } else {
                            logger.error("Failed to change access mode");
                            return ResponseEntity.internalServerError().build();
                        }
                    });
            });
    }

    public LocationController(
        LocationService locationService, AccessService accessService, UserService userService
    ) {

        this.locationService = locationService;
        this.accessService = accessService;
        this.userService = userService;
    }
}
