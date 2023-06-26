package com.example.location.controllers;

import com.example.location.dto.SharedLocation;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    @Autowired
    LocationService locationService;

    @Autowired
    AccessService accessService;

    @Autowired
    UserService userService;

    private static final String ACCESS_DENIED = "Access Denied";

    private static final String EMPTY = "empty";
    private static final String HEADER_NEEDED = "Authorization token is needed";

    @GetMapping(value = "/locations/all", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid) {
        if (uid.equals(EMPTY)) {
            logger.warn("Invalid or empty UID received");
            throw new BadRequestException(HEADER_NEEDED);
        }

        logger.info("Retrieving all locations for UID: {}", uid);
        return locationService.findAllLocations(uid)
            .thenApply(locations -> {
                logger.info("Retrieved all locations successfully");
                return ResponseEntity.ok(locations);
            });
    }

    @PostMapping(value = "/location", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Location>> saveLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestParam("name") String name,
        @RequestParam("address") String address) throws DbSaveException {
        if (uid.equals(EMPTY)) {
            logger.warn("Invalid or empty UID cookie received");
            throw new BadRequestException(HEADER_NEEDED);
        }

        logger.info("Saving location: {}", name);
        return locationService.saveLocation(uid, name, address)
            .thenApply(location -> {
                if (location.getLid() == null) {
                    logger.error("Failed to insert location to database");
                    throw new DbSaveException("Couldn't insert location to db");
                }

                logger.info("Location saved successfully");
                return ResponseEntity.ok(location);
            });
    }

    @PostMapping(value = "/location/{lid}/share", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestParam("email") String email, @RequestParam("shareMode") String shareMode,
        @PathVariable("lid") Long lid) throws DbSaveException {
        logger.info("Saving share for location with ID: " + lid);
        return accessService.saveAccess(email, shareMode, lid)
            .thenApply(access -> {
                if (access.getAid() == null) {
                    logger.error("Failed to insert access to database");
                    throw new DbSaveException("Couldn't insert access to db");
                }

                logger.info("Share saved successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(access);
            });
    }

    @GetMapping(value = "/location/{lid}", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Location>> getLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid) throws ForbidException, NoSuchElementException {
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

    @PostMapping(value = "/location/{lid}/unfriend", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Void>> unfriend(
        @PathVariable("lid") Long lid, @RequestParam("email") String email,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid) throws ForbidException {

        return userService.authorizeOwner(uid, lid)
            .thenCompose(authorized -> {
                if (Boolean.FALSE.equals(authorized)) {
                    logger.warn("User is not authorized to unfriend from the location");
                    throw new ForbidException(ACCESS_DENIED);
                }

                return accessService.delete(lid, email)
                    .thenApply(result -> {
                        if (result) {
                            logger.info("User successfully unfriended from the location");
                            return ResponseEntity.ok().build();
                        } else {
                            logger.error("Failed to unfriend user from the location");
                            return ResponseEntity.internalServerError().build();
                        }
                    });
            });
    }

    @PostMapping(value = "/location/{lid}/access", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Void>> changeMode(
        @PathVariable("lid") Long lid, @RequestParam("email") String email,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid) {

        return userService.authorizeOwner(uid, lid)
            .thenCompose(authorized -> {
                if (Boolean.FALSE.equals(authorized)) {
                    logger.warn("User is not authorized to change access mode for the location");
                    throw new ForbidException(ACCESS_DENIED);
                }

                return accessService.change(lid, email)
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

    public LocationController(LocationService locationService, AccessService accessService, UserService userService) {

        this.locationService = locationService;
        this.accessService = accessService;
        this.userService = userService;
    }
}
