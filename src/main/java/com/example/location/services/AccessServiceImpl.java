package com.example.location.services;

import com.example.location.dto.AccessDTO;
import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.UserRepository;
import com.example.location.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AccessServiceImpl implements AccessService {

    private static final Logger logger = LoggerFactory.getLogger(AccessServiceImpl.class);

    final
    AccessRepository accessRepository;

    final
    UserRepository userRepository;

    final String ACCESS_ADMIN = "admin";
    final String ACCESS_READ = "read-only";

    public CompletableFuture<Access> saveAccess(AccessDTO accessDTO) {

        Optional<User> user = userRepository.findByEmail(accessDTO.getEmail()).join();
        if (!user.isPresent()) {
            logger.warn("User not found: {}", accessDTO.getEmail());
            throw new BadRequestException("user not found");
        }

        return accessRepository.findByUidAndLid(user.get().getUid(), accessDTO.getLid())
            .thenCompose(access -> {
                if (access.isPresent()) {
                    Access saving = access.get();
                    if (!saving.getType().equals(accessDTO.getShareMode())) {
                        saving.setType(accessDTO.getShareMode());
                        logger.info("Access mode updated for user {} on location {}", accessDTO.getEmail(), accessDTO.getLid());
                    }
                    return accessRepository.save(saving);
                }

                logger.info("New access created for user {} on location {}", accessDTO.getEmail(), accessDTO.getLid());
                return accessRepository.save(new Access(null, user.get().getUid(), accessDTO.getLid(), accessDTO.getShareMode()));
            });
    }

    public CompletableFuture<List<UserAccessDto>> getUsersOnLocation(Long lid) {

        return accessRepository.getUserAccessByLocationId(lid);
    }

    public CompletableFuture<Boolean> delete(Long lid, String email) {

        return userRepository.findByEmail(email)
            .thenCompose(user -> {
                if (!user.isPresent()) {
                    logger.warn("User not found: {}", email);
                    return CompletableFuture.completedFuture(0);
                }
                return accessRepository.deleteByUidAndLid(user.get().getUid(), lid);
            })
            .thenApply(rows -> {
                boolean deleted = rows != 0;
                if (deleted) {
                    logger.info("Access deleted for user {} on location {}", email, lid);
                } else {
                    logger.info("No access found for user {} on location {}", email, lid);
                }
                return deleted;
            });
    }

    public CompletableFuture<Boolean> change(Long lid, String email) {

        return userRepository.findByEmail(email)
            .thenCompose(user -> {
                if (!user.isPresent()) {
                    logger.warn("User not found: {}", email);
                    return CompletableFuture.completedFuture(Optional.empty());
                }
                return accessRepository.findByUidAndLid(user.get().getUid(), lid);
            })
            .thenApply(access -> {
                if (!access.isPresent()) {
                    logger.warn("No access found for user {} on location {}", email, lid);
                    return false;
                }

                Access changedAccess = changeAccess(access.get());
                boolean accessUpdated = accessRepository.update(changedAccess) != null;
                if (accessUpdated)
                    logger.info("Access mode changed for user {} on location {}", email, lid);
                else
                    logger.error("Failed to update access mode for user {} on location {}", email, lid);

                return accessUpdated;
            });
    }

    @Override
    public Void validateShareMode(String shareMode) throws BadRequestException {
        if (shareMode.equals(ACCESS_ADMIN) || shareMode.equals(ACCESS_READ)) {
            return null;
        }
        throw new BadRequestException("Invalid share mode");
    }

    private Access changeAccess(Access a) {

        if (a.getType().equals(ACCESS_ADMIN)) {
            a.setType(ACCESS_READ);
        } else {
            a.setType(ACCESS_ADMIN);
        }
        return a;
    }

    public AccessServiceImpl(AccessRepository accessRepository, UserRepository userRepository   ) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }
}
