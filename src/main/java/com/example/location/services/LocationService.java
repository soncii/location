package com.example.location.services;

import com.example.location.dto.LocationDTO;
import com.example.location.dto.SharedLocation;
import com.example.location.entities.Location;
import com.example.location.entities.User;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    AccessRepository accessRepository;
    @Autowired
    UserRepository userRepository;

    public List<LocationDTO> findUserLocations(String uidString) {
        Long uid = Long.parseLong(uidString);
        List<LocationDTO> result = new ArrayList<>();
        List<Location> locations = locationRepository.findAllByUid(uid);
        if (locations!=null) {
            for (Location l : locations) {
                LocationDTO dto = new LocationDTO(l);
                dto.setPermissions(accessRepository.findAllByLid(l.getLid()));
                result.add(dto);
            }
        }
        return result;
    }
    public Location saveLocation(String uid, String name, String address) {
        try {
            Long l = Long.parseLong(uid);
        } catch (NumberFormatException e) {
            return null;
        }
        if (!userRepository.findById(Long.parseLong(uid)).isPresent()) return null;
        Location location = new Location();
        location.setUid(Long.parseLong(uid));
        location.setName(name);
        location.setAddress(address);
        return locationRepository.save(location);
    }

    public Optional<Location> findById(Long lid) {
        return locationRepository.findById(lid);
    }

    public List<SharedLocation> findAllLocations(String uid) {
        if (uid.equals("empty")) return null;
        User user = userRepository.findById(Long.parseLong(uid)).get();
        List<SharedLocation> allSharedLocation = locationRepository.findAllSharedLocation(Long.parseLong(uid));
        List<Location> ownerLocations = locationRepository.findAllByUid(Long.parseLong(uid));
        for (Location l:ownerLocations) {
            SharedLocation sharedLocation = new SharedLocation(l, user.getEmail());
            allSharedLocation.add(sharedLocation);
        }
        return allSharedLocation;
    }

    public LocationService(LocationRepository locationRepository, AccessRepository accessRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }
}
