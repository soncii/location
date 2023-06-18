package com.example.location.entities;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "location")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lid;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    public Location(long uidL, String name, String address) {
        this.uid=uidL;
        this.name=name;
        this.address=address;
    }

    public Location() {
    }
    // constructors, getters and setters
}