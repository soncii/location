package com.example.location.services;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AccessServiceImpl implements AccessService {

    @Autowired
    AccessRepository accessRepository;

    @Autowired
    UserRepository userRepository;

    final String ACCESS_ADMIN = "admin";
    final String ACCESS_READ = "read-only";

    public CompletableFuture<Access> saveAccess(String email, String shareMode, Long lid) {
        Optional<User> user = userRepository.findByEmail(email).join();
        if (!user.isPresent()) return CompletableFuture.completedFuture(null);
        return accessRepository.findByUidAndLid(user.get().getUid(), lid)
                    .thenCompose(access -> {
                        if (access.isPresent()) {
                            Access saving = access.get();
                            if (!saving.getType().equals(shareMode)) saving.setType(shareMode);
                            return accessRepository.save(saving);
                        }
                        return accessRepository.save(new Access());
                    });
    }



    public CompletableFuture<List<UserAccessDto>> getUsersOnLocation(Long lid) {
        return accessRepository.getUserAccessByLocationId(lid);
    }

    public CompletableFuture<Boolean> delete(Long uid, Long lid, String email) {
        return userRepository.findByEmail(email)
                .thenCompose(user -> {
                    if (!user.isPresent()) return CompletableFuture.completedFuture(0);
                    return accessRepository.deleteByUidAndLid(user.get().getUid(), lid);
                })
                .thenApply(rows -> rows!=0);
    }

    public CompletableFuture<Boolean> change(Long lid, String email) {
        return userRepository.findByEmail(email)
                .thenCompose(user -> {
                    if (!user.isPresent()) return CompletableFuture.completedFuture(Optional.empty());
                    return accessRepository.findByUidAndLid(user.get().getUid(), lid);
                })
                .thenApply(access -> {
                    if (!access.isPresent()) return false;
                    Access changedAccess = changeAccess(access.get());
                    return accessRepository.update(changedAccess) != null;
                });
    }

    private Access changeAccess(Access a) {
        if (a.getType().equals(ACCESS_ADMIN)) {
            a.setType(ACCESS_READ);
        } else {
            a.setType(ACCESS_ADMIN);
        }
        return a;
    }

    public AccessServiceImpl(AccessRepository accessRepository, UserRepository userRepository) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }
}
