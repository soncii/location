package com.example.location.controllers;

import com.example.location.entities.User;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@Controller
public class MainController {
    private final UserService userService;
    private final LocationService locationService;

    @Autowired
    public MainController(UserService userService, LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
    }


    @GetMapping("/register")
    public String registerGet(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/")
    public CompletableFuture<String> index(@CookieValue(value = "user", defaultValue = "empty") String uid, Model model) {
        if (uid.equals("empty")) {
            return CompletableFuture.completedFuture("indexNotLogged");
        }
        return locationService.findUserLocations(uid)
                .thenApply(locations -> {
                    model.addAttribute("locations", locations);
                    return "locations";
                });
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public CompletableFuture<String> loginUser(@RequestParam("email") String email, @RequestParam("password") String password,
                                               HttpServletResponse response) {
        return userService.authorize(email, password)
                .thenCompose(user -> {
                    if (user.isPresent()) {
                        Cookie cookie = new Cookie("user", user.get().getUid().toString());
                        cookie.setMaxAge(60 * 60 * 24 * 7);
                        response.addCookie(cookie);
                        return CompletableFuture.completedFuture("redirect:/");
                    } else {
                        return CompletableFuture.completedFuture("redirect:/login?error=true");
                    }
                });

    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        return userService.insertUser(user)
                .thenApply(saved -> "redirect:/login")
                .join();
    }
}
