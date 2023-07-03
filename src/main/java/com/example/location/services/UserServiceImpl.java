package com.example.location.services;

import com.example.location.entities.User;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    public CompletableFuture<Optional<User>> authorize(String email, String password) {

        if (email == null || password == null) {
            logger.warn("Invalid email or password");
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (!isValidEmail(email)) {
            logger.warn("Invalid email format: {}", email);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> insertUser(User user) {

        if (isEmpty(user)) {
            logger.warn("User is empty");
            CompletableFuture.completedFuture(user);
        }

        return userRepository.save(user);
    }

    @Override
    public CompletableFuture<Optional<User>> findUserById(Long uid) {

        return userRepository.findById(uid);
    }

    @Override
    public CompletableFuture<Boolean> authorizeOwner(String uidString, Long lid) {

        Long uid = Long.parseLong(uidString);
        if (uid == null) return CompletableFuture.completedFuture(false);
        return locationRepository.findByUidAndLid(uid, lid).thenApply(Optional::isPresent);
    }

    @Override
    public CompletableFuture<Boolean> deleteUser(Long uid) {

        return userRepository.deleteById(uid);
    }

    private boolean isEmpty(User user) {

        return (user.getFirstName() == null || user.getLastName() == null || user.getPassword() == null || user.getEmail() == null);
    }

    private boolean isValidEmail(String email) {

        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
