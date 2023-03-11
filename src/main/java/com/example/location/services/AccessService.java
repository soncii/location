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

@Service
public class AccessService {
    @Autowired
    AccessRepository accessRepository;
    @Autowired
    UserRepository userRepository;
    final String ACCESS_ADMIN = "admin";
    final String ACCESS_READ = "read-only";
    public Optional<Access> saveAccess(String email, String shareMode, Long lid) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Optional<Access> accessFromRepository = accessRepository.findByUidAndLid(user.get().getUid(), lid);
            if (accessFromRepository.isPresent()) {
                Access saving = accessFromRepository.get();
                if (!saving.getType().equals(shareMode)) saving.setType(shareMode);
                return Optional.of(accessRepository.save(saving));
            }
            Access access = new Access();
            access.setUid(user.get().getUid());
            access.setLid(lid);
            access.setType(shareMode);
            Access save = accessRepository.save(access);
            if (save.getAid()==null) return Optional.empty();
            return Optional.of(save);
        }
        return Optional.empty();
    }
    public List<UserAccessDto> getUsersOnLocation(Long lid) {
        return accessRepository.getUserAccessByLocationId(lid);
    }

    public boolean delete(Long uid, Long lid, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return accessRepository.deleteByUidAndLid(user.get().getUid(), lid)!=null;
        } else return false;
    }

    public boolean change(Long lid, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) return false;
        Long uid = user.get().getUid();
        Optional<Access> byUidAndLid = accessRepository.findByUidAndLid(uid, lid);
        if (!byUidAndLid.isPresent()) return false;
        Access access = changeAccess(byUidAndLid.get());
        accessRepository.save(access);
        return true;
    }
    private Access changeAccess(Access a) {
        if (a.getType().equals(ACCESS_ADMIN)) a.setType(ACCESS_READ);
        else a.setType(ACCESS_ADMIN);
        return a;

    }

    public AccessService(AccessRepository accessRepository, UserRepository userRepository) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }
}
