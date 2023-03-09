package com.example.location.controllers;

import com.example.location.dto.SharedLocation;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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
        if (uid.equals("empty")) return "redirect:/login";
        return "newLocation";
    }
    @GetMapping("/locations/all")
    public String allLocations(@CookieValue(value = "user", defaultValue = "empty") String uid, Model model) {
        List<SharedLocation> list = locationService.findAllLocations(uid);
        model.addAttribute("locations",list);
        return "sharedLocations";
    }
    @PostMapping("/locations")
    public String saveLocation(@CookieValue(value = "user", defaultValue = "empty") String uid,
            @RequestParam("name") String name, @RequestParam("address") String address) {
        Location location = locationService.saveLocation(uid, name, address);
        if (location.getLid()==null) return "error";
        return "redirect:/";
    }
    @PostMapping("/locations/{lid}/share")
    public String saveShare(@RequestParam("email") String email, @RequestParam("shareMode") String shareMode,
                            @PathVariable("lid") Long lid,
                            @CookieValue(value = "user", defaultValue = "empty") String uid) {

        accessService.saveAccess(email,shareMode,lid);
        return "redirect:/";
    }
    @GetMapping("/locations/{lid}")
    public String getLocation(@PathVariable("lid") Long lid, Model model,
                              @CookieValue(value = "user", defaultValue = "empty") String uid) {
        if (!userService.authorizeOwner(uid, lid)) return "error";
        model.addAttribute("location", locationService.findById(lid).get());
        model.addAttribute("friends", accessService.getUsersOnLocation(lid));
        return "location";
    }
    @PostMapping("/location/unfriend")
    public ResponseEntity<String> unfriend(@RequestParam("lid") Long lid, @RequestParam("email") String email,
                           @CookieValue(value = "user", defaultValue = "empty") String uidString) {
        if (!userService.authorizeOwner(uidString, lid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to perform this action.");
        }
        Long uid = Long.parseLong(uidString);
        boolean result = accessService.delete(uid,lid,email);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build();
    }
    @PostMapping("/location/access")
    public ResponseEntity<String> changeMode(@RequestParam("lid") Long lid, @RequestParam("email") String email,
                           @CookieValue(value="user", defaultValue = "empty") String uidString) {
        if (!userService.authorizeOwner(uidString, lid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to perform this action.");
        }
        boolean result = accessService.change(lid,email);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build();
    }
}
