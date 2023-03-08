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
    public Optional<Access> saveAccess(String email, String shareMode, Long lid) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
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
}
