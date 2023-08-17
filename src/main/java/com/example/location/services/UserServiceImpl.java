package com.example.location.services;

import com.example.location.component.HistoryEventPublisher;
import com.example.location.entities.Access;
import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import com.example.location.util.BadRequestException;
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
    private final AccessRepository accessRepository;

    private final HistoryEventPublisher historyEventPublisher;

    @Override
    public CompletableFuture<Optional<User>> authorize(String email, String password) {



        log.info("Logging in user with email: {}", Util.hideEmail(email));
        if (email == null || password == null) {
            log.warn("Invalid email or password");
            throw new BadRequestException("Invalid email or password");
        }

        if (!isValidEmail(email)) {
            log.warn("Invalid email format: {}", email);
            throw new BadRequestException("Invalid email format");
        }

        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> insertUser(User user) {

        log.info("Registering user with email: {}", Util.hideEmail(user.getEmail()));
        if (isEmpty(user)) {
            log.warn("User is empty");
            throw new BadRequestException("Fill all fields");
        }

        return userRepository.save(user).thenApply(saved -> {
            if (saved.getUid() == null) {
                log.error("User not saved {}", user);
                throw new DbException("User not saved");
            }
            historyEventPublisher.publishHistoryCreatedEvent(saved.getUid(), Util.ObjectType.USER, saved);
            return saved;
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findUserById(Long uid) {

        return userRepository.findById(uid);
    }

    @Override
    public CompletableFuture<Boolean> authorizeOwnerOrAdmin(Long uid, Long lid) {

        CompletableFuture<Optional<Location>> owner = locationRepository.findByUidAndLid(uid, lid);
        CompletableFuture<Optional<Access>> admin = accessRepository.findByUidAndLid(uid, lid);

        return owner.thenCombine(admin,
                (o, a) -> o.isPresent() || a.isPresent() && a.get().getType().equals(Util.AccessType.ADMIN.getValue()))
            .thenCompose(
                authorized -> {
                    if (authorized) {
                        return CompletableFuture.completedFuture(true);
                    }
                    log.warn("User not authorized {uid: {}, lid: {}}", uid, lid);
                    return CompletableFuture.completedFuture(false);
                }
            ).exceptionally(
                throwable -> {
                    log.error("Error authorizing user: {}", throwable.getMessage());
                    return false;
                }
            );
    }

    @Override
    public CompletableFuture<Boolean> deleteUser(Long uid) {

        return userRepository.deleteById(uid).thenApply(isDeleted -> {
            if (!isDeleted) {
                log.warn("User not found for ID: {}", uid);
                throw new DbException("Could not delete user");
            }
            log.info("User deleted successfully");
            historyEventPublisher.publishHistoryDeletedEvent(uid, Util.ObjectType.USER, uid);
            return isDeleted;
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
