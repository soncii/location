package com.example.location.controllers;

import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.services.LocationService;
import com.example.location.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {
    @Autowired
    UserService userService;
    @Autowired
    LocationService locationService;
    @GetMapping("/register")
    public String registerGet(Model model) {
        User user = new User();
        model.addAttribute("user",user);
        return "register";
    }
    @GetMapping("/")
    public String index(@CookieValue(value = "user", defaultValue = "empty") String uid, Model model) {
        if (uid.equals("empty"))
            return "indexNotLogged";
        model.addAttribute("locations", locationService.findUserLocations(uid));
        return "locations";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email, @RequestParam("password") String password,
                            HttpServletResponse response) {
        Optional<User> user = userService.authorize(email, password);
        if (user.isPresent()) {
            Cookie cookie = new Cookie("user", user.get().getUid().toString());
            cookie.setMaxAge(60*60*24*7);
            response.addCookie(cookie);
            return "redirect:/";
        } else return "redirect:/login?error=true";

    }
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        User saved = userService.insertUser(user);
        return "redirect:/login";
    }
    @GetMapping("/test")
    public String test() {
        userService.findUserById(1L);
        return "redirect:/register";
    }
}
