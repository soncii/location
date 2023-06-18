package com.example.location.controllers;

import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class LocationController {
    @Autowired
    LocationService locationService;

    @Autowired
    AccessService accessService;

    @Autowired
    UserService userService;

    @GetMapping("/locations/new")
    public String addLocation(@CookieValue(value = "user", defaultValue = "empty") String uid) {
        if (uid.equals("empty")) {
            return "redirect:/login";
        }
        return "newLocation";
    }

    @GetMapping("/locations/all")
    public CompletableFuture<String> allLocations(@CookieValue(value = "user", defaultValue = "empty") String uid, Model model) {
        return locationService.findAllLocations(uid)
                .thenApply(locations -> {
                    model.addAttribute("locations", locations);
                    return "sharedLocations";
                });
    }

    @PostMapping("/locations")
    public CompletableFuture<String> saveLocation(@CookieValue(value = "user", defaultValue = "empty") String uid,
                                                  @RequestParam("name") String name,
                                                  @RequestParam("address") String address) {
        return locationService.saveLocation(uid, name, address)
                .thenApply(location -> {
                    System.out.println(location);
                    if (location.getLid() == null) {
                        return "error";
                    }
                    return "redirect:/";
                });
    }

    @PostMapping("/locations/{lid}/share")
    public String saveShare(@RequestParam("email") String email, @RequestParam("shareMode") String shareMode,
                            @PathVariable("lid") Long lid,
                            @CookieValue(value = "user", defaultValue = "empty") String uid) {

        accessService.saveAccess(email, shareMode, lid);
        return "redirect:/";
    }

    @GetMapping("/locations/{lid}")
    @Async
    public CompletableFuture<String> getLocation(
            @PathVariable("lid") Long lid,
            Model model,
            @CookieValue(value = "user", defaultValue = "empty") String uid
    ) {
        return  userService.authorizeOwner(uid, lid)
                .thenCompose((authorized -> {
                    if (Boolean.FALSE.equals(authorized)) return CompletableFuture.completedFuture(Optional.empty());
                    return locationService.findById(lid);
                }))
                .thenApply(location -> {
                    if (!location.isPresent()) {
                        return "error";
                    }
                    model.addAttribute("location", location.get());
                    model.addAttribute("friends", accessService.getUsersOnLocation(lid));
                    return "location";
                });
    }


    @PostMapping("/location/unfriend")
    public CompletableFuture<ResponseEntity<String>> unfriend(@RequestParam("lid") Long lid, @RequestParam("email") String email,
                                                              @CookieValue(value = "user", defaultValue = "empty") String uidString) {

        return userService.authorizeOwner(uidString, lid)
                .thenCompose(authorized -> {
                    if (Boolean.FALSE.equals(authorized)) {
                        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to perform this action."));
                    }
                    Long uid = Long.parseLong(uidString);
                    return accessService.delete(uid, lid, email)
                            .thenApply(result -> ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build());
                });
    }

    @PostMapping("/location/access")
    public CompletableFuture<ResponseEntity<String>> changeMode(@RequestParam("lid") Long lid, @RequestParam("email") String email,
                                                                @CookieValue(value = "user", defaultValue = "empty") String uidString) {
        return userService.authorizeOwner(uidString, lid)
                .thenCompose(authorized -> {
                    if (Boolean.FALSE.equals(authorized)) {
                        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to perform this action."));
                    }
                    return accessService.change(lid, email)
                            .thenApply(result -> ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build());
                });
    }

    public LocationController(LocationService locationService, AccessService accessService, UserService userService) {
        this.locationService = locationService;
        this.accessService = accessService;
        this.userService = userService;
    }
}
