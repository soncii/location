package com.example.location.controllers;

import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.services.AccessService;
import com.example.location.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LocationController {
    @Autowired
    LocationService locationService;
    @Autowired
    AccessService accessService;
    @GetMapping("/locations/new")
    public String addLocation(@CookieValue(value = "user", defaultValue = "empty") String uid) {
        if (uid.equals("empty")) return "redirect:/login";
        return "newLocation";
    }
    @PostMapping("/locations")
    public String saveLocation(@CookieValue(value = "user", defaultValue = "empty") String uid,
            @RequestParam("name") String name, @RequestParam("address") String address) {
        locationService.saveLocation(uid, name, address);
        return "redirect:/";
    }
    @PostMapping("/locations/{lid}/share")
    public String saveShare(@RequestParam("email") String email, @RequestParam("shareMode") String shareMode,
                            @PathVariable("lid") Long lid) {
        accessService.saveAccess(email,shareMode,lid);
        return "redirect:/";
    }
    @GetMapping("/locations/{lid}")
    public String getLocation(@PathVariable("lid") Long lid, Model model) {
        model.addAttribute("location", locationService.findById(lid).get());
        model.addAttribute("friends", accessService.getUsersOnLocation(lid));
        return "location";
    }
}
