package com.example.location.services;

import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    LocationRepository locationRepository;

    public boolean isEmpty(User user) {
        return (user.getFirstName()==null || user.getLastName()==null
                || user.getPassword()==null || user.getEmail()==null);
    }
    public User insertUser(User user) {
        if (isEmpty(user)) return user;
        return userRepository.save(user);
    }
    public Optional<User> findUserById(Long uid) {
        return userRepository.findById(uid);
    }

    public Optional<User> authorize(String email, String password) {
        if (email==null || password ==null) return Optional.empty();
        return userRepository.findByEmailAndPassword(email, password);
    }

    public boolean authorizeOwner(String uidString, Long lid) {
        if (uidString.equals("empty")) return false;
        Long uid;
        try {
            uid = Long.parseLong(uidString);
        } catch (Exception e) {
            return false;
        }
        Optional<Location> byIdAndLid = locationRepository.findByUidAndLid(uid, lid);
        return byIdAndLid.isPresent();
    }
}
