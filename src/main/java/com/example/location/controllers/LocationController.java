package com.example.location.controllers;

import com.example.location.annotation.AuthorizationOwner;
import com.example.location.annotation.AuthorizationRequired;
import com.example.location.dto.AccessDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.dto.UserLocationDTO;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@Log4j2
@RequestMapping("/location")
public class LocationController {

    private static final String EMPTY = "0";
    private final LocationService locationService;
    private final AccessService accessService;
    private final UserService userService;

    @GetMapping("/{lid}")
    @AuthorizationRequired
    @AuthorizationOwner
    public CompletableFuture<ResponseEntity<Optional<Location>>> getLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @PathVariable("lid") Long lid
    ) {

        return locationService.findById(lid).thenApply(ResponseEntity::ok);
    }

    @PostMapping("")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Location>> saveLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @RequestBody Location location
    ) {

        location.setUid(uid);
        return locationService.saveLocation(location).thenApply(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @DeleteMapping("/{lid}")
    @AuthorizationRequired
    @AuthorizationOwner
    public CompletableFuture<ResponseEntity<Void>> deleteLocation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @PathVariable("lid") Long lid
    ) {

        return locationService.deleteById(lid)
            .thenApply(result -> ResponseEntity.ok().build());
    }

    @GetMapping("/all")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid
    ) {

        return locationService.findAllLocations(uid).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/share")
    @AuthorizationRequired
    @AuthorizationOwner
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @RequestBody AccessDTO access
    ) {

        accessService.validateShareMode(access.getShareMode());
        return accessService.saveAccess(access)
            .thenApply(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @PostMapping("/unfriend")
    @AuthorizationRequired
    @AuthorizationOwner
    public CompletableFuture<ResponseEntity<Void>> unfriend(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @RequestBody UserLocationDTO userLocation
    ) {

        return accessService.delete(uid, userLocation.getLid(), userLocation.getEmail())
            .thenApply(result -> ResponseEntity.ok().build());
    }

    @PostMapping("/access")
    @AuthorizationRequired
    @AuthorizationOwner
    public CompletableFuture<ResponseEntity<Void>> changeMode(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) Long uid,
        @RequestBody UserLocationDTO userLocation

    ) {

        return accessService.change(uid, userLocation.getLid(), userLocation.getEmail())
            .thenApply(result -> ResponseEntity.ok().build());
    }
}
