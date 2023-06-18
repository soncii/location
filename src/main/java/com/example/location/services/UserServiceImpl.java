package com.example.location.services;

import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public CompletableFuture<Optional<User>> authorize(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null || password == null) {
                return Optional.empty();
            }
            if (!isValidEmail(email)) {
                return Optional.empty();
            }
            return userRepository.findByEmailAndPassword(email, password);
        });
    }

    @Override
    public CompletableFuture<User> insertUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            if (isEmpty(user)) {
                return user;
            }
            return userRepository.save(user);
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findUserById(Long uid) {
        return CompletableFuture.supplyAsync(() -> userRepository.findById(uid));
    }

    @Override
    public CompletableFuture<Boolean> authorizeOwner(String uidString, Long lid) {
        return CompletableFuture.supplyAsync(() -> {
            Long uid;
            try {
                uid = Long.parseLong(uidString);
            } catch (Exception e) {
                return false;
            }
            Optional<Location> byIdAndLid = locationRepository.findByUidAndLid(uid, lid);
            return byIdAndLid.isPresent();
        });
    }

    private boolean isEmpty(User user) {
        return (user.getFirstName() == null || user.getLastName() == null
                || user.getPassword() == null || user.getEmail() == null);
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
