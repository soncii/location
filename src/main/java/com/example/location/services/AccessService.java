package com.example.location.services;

import com.example.location.dto.AccessDTO;
import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import com.example.location.util.BadRequestException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AccessService {

    CompletableFuture<Access> saveAccess(AccessDTO access);

    CompletableFuture<List<UserAccessDto>> getUsersOnLocation(Long lid);

    CompletableFuture<Boolean> delete(Long lid, String email);

    CompletableFuture<Boolean> change(Long lid, String email);

    Void validateShareMode(String shareMode) ;
}
