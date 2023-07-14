package com.example.location.services;

import com.example.location.dto.AccessDTO;
import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.UserRepository;
import com.example.location.util.BadRequestException;
import com.example.location.util.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Log4j2
public class AccessServiceImpl implements AccessService {

    private final AccessRepository accessRepository;

    private final UserRepository userRepository;

    static final String ACCESS_ADMIN = "admin";
    static final String ACCESS_READ = "read-only";

    public CompletableFuture<Access> saveAccess(AccessDTO accessDTO) {

        return userRepository.findByEmail(accessDTO.getEmail()).thenCompose(user -> {
            if (!user.isPresent()) {
                log.warn("User not found: {}", accessDTO.getEmail());
                throw new NotFoundException("User not found");
            }
            return accessRepository.findByUidAndLid(user.get().getUid(), accessDTO.getLid()).thenCompose(access -> {
                if (access.isPresent()) {
                    Access saving = access.get();
                    if (!saving.getType().equals(accessDTO.getShareMode())) {
                        saving.setType(accessDTO.getShareMode());
                        log.info("Access mode updated for user {} on location {}", accessDTO.getEmail(),
                            accessDTO.getLid());
                    }
                    return accessRepository.save(saving);
                }

                log.info("New access created for user {} on location {}", accessDTO.getEmail(), accessDTO.getLid());
                return accessRepository.save(new Access(null, user.get().getUid(), accessDTO.getLid(),
                    accessDTO.getShareMode()));
            });
        });
    }

    public CompletableFuture<List<UserAccessDto>> getUsersOnLocation(Long lid) {

        return accessRepository.getUserAccessByLocationId(lid);
    }

    public CompletableFuture<Boolean> delete(Long lid, String email) {

        return userRepository.findByEmail(email).thenCompose(user -> {
            if (!user.isPresent()) {
                log.warn("User not found: {}", email);
                return CompletableFuture.completedFuture(0);
            }
            return accessRepository.deleteByUidAndLid(user.get().getUid(), lid);
        }).thenApply(rows -> {
            boolean deleted = rows != 0;
            if (deleted) {
                log.info("Access deleted for user {} on location {}", email, lid);
            } else {
                log.info("No access found for user {} on location {}", email, lid);
            }
            return deleted;
        });
    }

    public CompletableFuture<Boolean> change(Long lid, String email) {

        return userRepository.findByEmail(email).thenCompose(user -> {
            if (!user.isPresent()) {
                log.warn("User not found: {}", email);
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return accessRepository.findByUidAndLid(user.get().getUid(), lid);
        }).thenApply(access -> {
            if (!access.isPresent()) {
                log.warn("No access found for user {} on location {}", email, lid);
                return false;
            }

            Access changedAccess = changeAccess(access.get());
            boolean accessUpdated = accessRepository.update(changedAccess) != null;
            if (accessUpdated) log.info("Access mode changed for user {} on location {}", email, lid);
            else log.error("Failed to update access mode for user {} on location {}", email, lid);

            return accessUpdated;
        });
    }

    @Override
    public Void validateShareMode(String shareMode) {

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
}
