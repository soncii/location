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

    public Optional<Access> saveAccess(String email, String shareMode, Long lid) {
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) return Optional.empty();
        Optional<Access> accessFromRepository = accessRepository.findByUidAndLid(user.get().getUid(), lid);
        if (accessFromRepository.isPresent()) {
            Access saving = accessFromRepository.get();
            if (!saving.getType().equals(shareMode)) saving.setType(shareMode);
            return Optional.of(accessRepository.save(saving));
        }
        Access access = new Access(user.get().getUid(),lid,shareMode);
        Access save = accessRepository.save(access);
        if (save.getAid()==null) return Optional.empty();
        return Optional.of(save);

    }



    public List<UserAccessDto> getUsersOnLocation(Long lid) {
        return accessRepository.getUserAccessByLocationId(lid);
    }

    public CompletableFuture<Boolean> delete(Long uid, Long lid, String email) {
        return userRepository.findByEmail(email)
                .map(user -> CompletableFuture.supplyAsync(() -> accessRepository.deleteByUidAndLid(user.getUid(), lid) != 0))
                .orElse(CompletableFuture.completedFuture(false));
    }

    public CompletableFuture<Boolean> change(Long lid, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) return CompletableFuture.completedFuture(false);
        Long uid = user.get().getUid();
        return accessRepository.findByUidAndLid(uid, lid)
                .map(access -> {
                    Access changedAccess = changeAccess(access);
                    return CompletableFuture.completedFuture(accessRepository.update(changedAccess) != null);
                })
                .orElseGet(() -> CompletableFuture.completedFuture(false));
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
