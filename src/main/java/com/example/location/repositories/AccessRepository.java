package com.example.location.repositories;

import com.example.location.dto.UserAccessDto;
import com.example.location.entities.Access;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AccessRepository {

    CompletableFuture<List<Access>> findAllByLid(Long lid);

    CompletableFuture<List<UserAccessDto>> getUserAccessByLocationId(Long lid);

    CompletableFuture<Integer> deleteByUidAndLid(Long uid, Long lid);

    CompletableFuture<Optional<Access>> findByUidAndLid(Long uid, Long lid);

    CompletableFuture<Access> save(Access a);

    CompletableFuture<Boolean> update(Access a);
}
