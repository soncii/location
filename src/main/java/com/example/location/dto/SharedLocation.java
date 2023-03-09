package com.example.location.dto;

import com.example.location.entities.Location;
import lombok.Data;

@Data
public class SharedLocation {
    private Long lid;


    private String email;

    private String name;

    private String address;
    private String accessType;

    public SharedLocation(Long lid, String email, String name, String address, String accessType) {
        this.lid = lid;
        this.email = email;
        this.name = name;
        this.address = address;
        this.accessType = accessType;
    }

    public SharedLocation(Location l, String email) {
        this.lid= l.getLid();
        this.email=email;
        this.name=l.getName();
        this.address=l.getAddress();
        this.accessType="owner";
    }
}
