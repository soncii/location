package com.example.location.services;

import com.example.location.dto.LocationDTO;
import com.example.location.entities.Location;
import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.LocationRepository;
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

    public List<LocationDTO> findUserLocations(String uidString) {
        Long uid = Long.parseLong(uidString);
        List<Location> locations = locationRepository.findAllByUid(uid);

        List<LocationDTO> result = new ArrayList<>();
        for (Location l:locations) {
            LocationDTO dto = new LocationDTO(l);
            dto.setPermissions(accessRepository.findAllByLid(l.getLid()));
            result.add(dto);
        }
        return result;
    }
    public Location saveLocation(String uid, String name, String address) {
        Location location = new Location();
        location.setUid(Long.parseLong(uid));
        location.setName(name);
        location.setAddress(address);
        return locationRepository.save(location);
    }

    public Optional<Location> findById(Long lid) {
        return locationRepository.findById(lid);
    }
}
