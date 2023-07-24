package com.example.location.controllers;

import com.example.location.annotation.AuthorizationRequired;
import com.example.location.dto.LocationDTO;
import com.example.location.dto.LoginDTO;
import com.example.location.entities.User;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import com.example.location.util.UnauthorizedException;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@Log4j2
public class MainController {

    private final UserService userService;
    private final LocationService locationService;

    @GetMapping("/user/locations")
    @AuthorizationRequired
    public CompletableFuture<ResponseEntity<List<LocationDTO>>> index(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "empty") String uid
    ) {

        return locationService.findUserLocations(uid)
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<Void>> loginUser(
        @RequestBody LoginDTO login, HttpServletResponse response
    ) {

        return userService.authorize(login.getEmail(), login.getPassword()).thenApply(user -> {
            if (!user.isPresent()) throw new UnauthorizedException();
            response.addHeader(HttpHeaders.AUTHORIZATION, user.get().getUid().toString());
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<User>> registerUser(@RequestBody User user) {

        return userService.insertUser(user)
            .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/user/{uid}")
    public CompletableFuture<ResponseEntity> deleteUser(@PathVariable("uid") Long uid) {

        return userService.deleteUser(uid)
            .thenApply(ResponseEntity::ok);
    }
}
