package com.example.location.controllers;

import com.example.location.dto.LocationDTO;
import com.example.location.dto.LoginDTO;
import com.example.location.entities.User;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import com.example.location.util.BadRequestException;
import com.example.location.util.DbSaveException;
import com.example.location.util.ForbidException;

import com.example.location.util.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final UserService userService;
    private final LocationService locationService;

    @GetMapping("/user/locations")
    public CompletableFuture<ResponseEntity<List<LocationDTO>>> index(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "empty") String uid
    ) {

        if (uid.equals("empty")) {
            logger.warn("Invalid or empty UID cookie received");
            throw new BadRequestException();
        }

        logger.info("Retrieving locations for UID: {}", uid);
        return locationService.findUserLocations(uid).thenApply(locations -> {
            logger.info("Retrieved locations successfully");
            return ResponseEntity.ok(locations);
        });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<Void>> loginUser(
        @RequestBody LoginDTO login, HttpServletResponse response
    )  {

        logger.info("Logging in user with email: {}", login.getEmail());
        logger.info("password: {}", login.getPassword());
        return userService.authorize(login.getEmail(), login.getPassword()).thenCompose(user -> {
            if (!user.isPresent()) {
                logger.warn("Invalid email or password");
                throw new UnauthorizedException();
            }

            logger.info("User logged in successfully");
            response.addHeader(HttpHeaders.AUTHORIZATION, user.get().getUid().toString());
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.OK));
        });
    }

    @PostMapping(value = "/register")
    public CompletableFuture<ResponseEntity<User>> registerUser(@RequestBody User user) {

        logger.info("Registering user with email: {}", user.getEmail());
        return userService.insertUser(user).thenApply(saved -> {
            if (saved.getUid() == null) {
                logger.error("Failed to insert user to database");
                throw new DbSaveException();
            }

            logger.info("User registered successfully");
            return ResponseEntity.ok(saved);
        });
    }

    @DeleteMapping("/user/{uid}")
    public CompletableFuture<ResponseEntity> deleteUser(@PathVariable("uid") Long uid) {

        return userService.deleteUser(uid).thenApply(deleted -> {
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().build();
        });
    }
}
