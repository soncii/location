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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final UserService userService;
    private final LocationService locationService;
    private static final String COOKIE_NEEDED = "cookie value is needed";

    public MainController(UserService userService, LocationService locationService) {

        this.userService = userService;
        this.locationService = locationService;
    }

    @GetMapping("/user/locations")
    public CompletableFuture<ResponseEntity<List<LocationDTO>>> index(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "empty") String uid
    ) throws BadRequestException {

        if (uid.equals("empty")) {
            logger.warn("Invalid or empty UID cookie received");
            throw new BadRequestException(COOKIE_NEEDED);
        }

        logger.info("Retrieving locations for UID: {}", uid);
        return locationService.findUserLocations(uid).thenApply(locations -> {
            logger.info("Retrieved locations successfully");
            return ResponseEntity.ok(locations);
        });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<Void>> loginUser(
        LoginDTO login, HttpServletResponse response
    ) throws ForbidException {

        logger.info("Logging in user with email: {}", login.getEmail());
        return userService.authorize(login.getEmail(), login.getPassword()).thenCompose(user -> {
            if (!user.isPresent()) {
                logger.warn("Invalid email or password");
                throw new UnauthorizedException("email or password is incorrect");
            }

            logger.info("User logged in successfully");
            response.addHeader(HttpHeaders.AUTHORIZATION, user.get().getUid().toString());
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.OK));
        });
    }

    @PostMapping(value = "/register")
    public CompletableFuture<ResponseEntity<User>> registerUser(User user) {

        logger.info("Registering user with email: {}", user.getEmail());

        return userService.insertUser(user).thenApply(saved -> {
            if (saved.getUid() == null) {
                logger.error("Failed to insert user to database");
                throw new DbSaveException("couldn't insert user to db");
            }

            logger.info("User registered successfully");
            return ResponseEntity.ok(saved);
        });
    }
}
