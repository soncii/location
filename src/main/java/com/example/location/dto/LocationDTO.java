package com.example.location.dto;

import com.example.location.entities.Access;
import com.example.location.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LocationDTO {

    private Long id;

    private Long uid;

    private String name;

    private String address;
    private List<Access> permissions;

    public LocationDTO(Location l, List<Access> arrayList) {

        this.id = l.getLid();
        this.uid = l.getUid();
        this.name = l.getName();
        this.address = l.getAddress();
        this.permissions = arrayList;
    }
}
