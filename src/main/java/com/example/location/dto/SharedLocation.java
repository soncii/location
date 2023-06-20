package com.example.location.dto;

import com.example.location.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedLocation {
    private Long lid;


    private String email;

    private String name;

    private String address;
    private String accessType;

    public SharedLocation(Location l, String email) {
        this.lid= l.getLid();
        this.email=email;
        this.name=l.getName();
        this.address=l.getAddress();
        this.accessType="owner";
    }
}
