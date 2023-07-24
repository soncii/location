package com.example.location.services;

import com.example.location.component.HistoryEventPublisher;
import com.example.location.entities.User;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import com.example.location.util.DbException;
import com.example.location.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    private final HistoryEventPublisher historyEventPublisher;

    @Override
    public CompletableFuture<Optional<User>> authorize(String email, String password) {

        log.info("Logging in user with email: {}", Util.hideEmail(email));
        if (email == null || password == null) {
            log.warn("Invalid email or password");
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (!isValidEmail(email)) {
            log.warn("Invalid email format: {}", email);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> insertUser(User user) {

        log.info("Registering user with email: {}", Util.hideEmail(user.getEmail()));
        if (isEmpty(user)) {
            log.warn("User is empty");
            CompletableFuture.completedFuture(user);
        }

        return userRepository.save(user).thenCompose(saved -> {
            if (saved.getUid() == null) {
                log.error("User not saved {}", user);
                throw new DbException();
            }
            historyEventPublisher.publishHistoryCreatedEvent(saved.getUid(), "USER", saved);
            return CompletableFuture.completedFuture(saved);
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findUserById(Long uid) {

        return userRepository.findById(uid);
    }

    @Override
    public CompletableFuture<Boolean> authorizeOwner(String uidString, Long lid) {

        Long uid = Long.parseLong(uidString);
        return locationRepository.findByUidAndLid(uid, lid).thenApply(Optional::isPresent);
    }

    @Override
    public CompletableFuture<Boolean> deleteUser(Long uid) {

        return userRepository.deleteById(uid).thenCompose(deleted -> {
            if (deleted) {
                log.info("User deleted successfully");
                historyEventPublisher.publishHistoryDeletedEvent(uid, "USER", uid);
                return CompletableFuture.completedFuture(true);
            }
            log.warn("User not found for ID: {}", uid);
            throw new DbException();
        });
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
