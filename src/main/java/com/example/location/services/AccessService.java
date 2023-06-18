package com.example.location.services;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AccessService {
    Optional<Access> saveAccess(String email, String shareMode, Long lid);

    List<UserAccessDto> getUsersOnLocation(Long lid);

    CompletableFuture<Boolean> delete(Long uid, Long lid, String email);

    CompletableFuture<Boolean> change(Long lid, String email);
}
