package com.example.location.services;

import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (!isValidEmail(email)) return Optional.empty() ;
        return userRepository.findByEmailAndPassword(email, password);
    }
    public boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public boolean authorizeOwner(String uidString, Long lid) {
        Long uid;
        try {
            uid = Long.parseLong(uidString);
        } catch (Exception e) {
            return false;
        }
        Optional<Location> byIdAndLid = locationRepository.findByUidAndLid(uid, lid);
        return byIdAndLid.isPresent();
    }

    public UserService(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }
}
