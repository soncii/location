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
import com.example.location.util.ForbidException;
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
    public CompletableFuture<ResponseEntity<Optional<Location>>> getLocation(
        @PathVariable("lid") Long lid,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, lid)
            .thenCompose(authorized -> {
                if (Boolean.FALSE.equals(authorized)) throw new ForbidException();
                return locationService.findById(lid).thenApply(ResponseEntity::ok);
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

        return locationService.deleteById(lid)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/all")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<List<SharedLocation>>> allLocations(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return locationService.findAllLocations(uid).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/share")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<Access>> saveShare(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid,
        @RequestBody AccessDTO access
    ) {

        accessService.validateShareMode(access.getShareMode());
        return accessService.saveAccess(access)
            .thenApply(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @PostMapping("/unfriend")
    public CompletableFuture<ResponseEntity> unfriend(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid())
            .thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) throw new ForbidException();
            return accessService.delete(Long.parseLong(uid), userLocation.getLid(), userLocation.getEmail())
                .thenApply(ResponseEntity::ok);
        });
    }

    @PostMapping("/access")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity> changeMode(
        @RequestBody UserLocationDTO userLocation,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = EMPTY) String uid
    ) {

        return userService.authorizeOwner(uid, userLocation.getLid()).thenCompose(authorized -> {
            if (Boolean.FALSE.equals(authorized)) throw new ForbidException();
            return accessService.change(Long.parseLong(uid), userLocation.getLid(), userLocation.getEmail())
                .thenApply(ResponseEntity::ok);
        });
    }
}
