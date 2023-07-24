package com.example.location.controllers;

import com.example.location.annotation.AuthorizationRequired;
import com.example.location.component.HistoryEventPublisher;
import com.example.location.dto.AccessDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.dto.UserLocationDTO;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import com.example.location.util.DbException;
import com.example.location.util.ForbidException;
import com.example.location.util.NotFoundException;
import com.example.location.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@Log4j2
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    private final AccessService accessService;

    private final UserService userService;

    private final HistoryEventPublisher historyEventPublisher;

    private static final String EMPTY = "empty";
    private static final String LOCATION = "LOCATION";
    private static final String USER = "USERS";
    private static final String ACCESS = "ACCESS";

    @GetMapping("/{lid}")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Location>> getLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        log.info("Retrieving location with ID: {}", lid);
        return userService.authorizeOwner(uid, lid).thenCompose(auth -> {
            if (Boolean.FALSE.equals(auth)) {
                log.warn("User is not authorized to access the location");
                throw new ForbidException();
            }
            return locationService.findById(lid).thenApply(location -> {
                if (!location.isPresent()) {
                    log.error("Location not found");
                    throw new NotFoundException("Location");
                }

                log.info("Retrieved location successfully");
                return ResponseEntity.ok(location.get());
            });
        });
    }

    @PostMapping("")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Location>> saveLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestBody Location location
    ) {

        location.setUid(Long.parseLong(uid));
        return locationService.saveLocation(location).thenApply(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @DeleteMapping("/{lid}")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity> deleteLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return locationService.deleteById(lid).thenApply(deleted -> {
            if (Boolean.FALSE.equals(deleted)) {
                log.error("Failed to delete location {}", lid);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            historyEventPublisher.publishHistoryDeletedEvent(Long.parseLong(uid), LOCATION, lid);
            log.info("Location deleted successfully {}", lid);
            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("/all")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        log.info("Retrieving all locations for UID: {}", uid);
        return locationService.findAllLocations(uid).thenApply(locations -> {
            log.info("Retrieved all locations successfully");
            log.info("Locations: {}", locations);
            return ResponseEntity.ok(locations);
        });
    }

    @PostMapping("/share")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestBody AccessDTO access
    ) {

        accessService.validateShareMode(access.getShareMode());
        return accessService.saveAccess(access).thenApply(saved -> {
            if (saved.getAid() == null) {
                log.error("Failed to insert access to database");
                throw new DbException();
            }
            historyEventPublisher.publishHistoryCreatedEvent(Long.parseLong(uid), ACCESS, saved);
            log.info("Share saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        });
    }

    @PostMapping("/unfriend")
    public CompletableFuture<ResponseEntity<Void>> unfriend(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid()).thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) {
                log.warn("User is not authorized to unfriend from the location");
                throw new ForbidException();
            }

            return accessService.delete(Long.parseLong(uid), userLocation.getLid(), userLocation.getEmail()).thenApply(result -> {
                if (Boolean.TRUE.equals(result)) {
                    log.info("User {} successfully unfriended from the location {}",
                        Util.hideEmail(userLocation.getEmail()), userLocation.getLid());
                    historyEventPublisher.publishHistoryDeletedEvent(Long.parseLong(uid), ACCESS, userLocation);
                    return ResponseEntity.ok().build();
                } else {
                    log.error("Failed to unfriend user from the location");
                    throw new DbException();
                }
            });
        });
    }

    @PostMapping("/access")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Void>> changeMode(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid()).thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) {
                log.warn("User is not authorized to change access mode for the location");
                throw new ForbidException();
            }

            return accessService.change(Long.parseLong(uid), userLocation.getLid(), userLocation.getEmail()).thenApply(result -> {
                if (Boolean.TRUE.equals(result)) {
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.internalServerError().build();
                }
            });
        });
    }
}
