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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final AccessRepository accessRepository;
    private final UserRepository userRepository;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository, AccessRepository accessRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<List<LocationDTO>> findUserLocations(String uidString) {
        return CompletableFuture.supplyAsync(() -> {
            Long uid = Long.parseLong(uidString);
            List<Location> locations = locationRepository.findAllByUid(uid);
            if (locations == null) {
                return new ArrayList<>();
            }

            return locations.stream()
                    .map(l -> CompletableFuture.supplyAsync(() -> {
                        LocationDTO dto = new LocationDTO(l);
                        dto.setPermissions(accessRepository.findAllByLid(l.getLid()));
                        return dto;
                    }))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        });
    }



    @Override
    public CompletableFuture<Location> saveLocation(String uid, String name, String address) {
        return CompletableFuture.supplyAsync(() -> {
            long uidL;
            try {
                uidL = Long.parseLong(uid);
            } catch (NumberFormatException e) {
                return null;
            }
            if (!userRepository.findById(uidL).isPresent()) {
                return null;
            }
            Location location = new Location(uidL, name,address);
            return locationRepository.save(location);
        });
    }

    @Override
    public CompletableFuture<Optional<Location>> findById(Long lid) {
        return CompletableFuture.supplyAsync(() -> locationRepository.findById(lid));
    }

    @Override
    public CompletableFuture<List<SharedLocation>> findAllLocations(String uid) {
        return CompletableFuture.supplyAsync(() -> {
            if (uid.equals("empty")) {
                return null;
            }

            User user = userRepository.findById(Long.parseLong(uid)).orElse(null);

            List<SharedLocation> allSharedLocation = locationRepository.findAllSharedLocation(Long.parseLong(uid));
            List<Location> ownerLocations = locationRepository.findAllByUid(Long.parseLong(uid));

            return ownerLocations.stream()
                    .map(location -> CompletableFuture.supplyAsync(() -> {
                        String userEmail = (user != null) ? user.getEmail() : null;
                        return new SharedLocation(location, userEmail);
                    }))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        });
    }

}