package com.example.location.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    private Long lid;
    private Long uid;
    private String name;
    private String address;

    public Location(long uidL, String name, String address) {

        this.uid = uidL;
        this.name = name;
        this.address = address;
    }
}