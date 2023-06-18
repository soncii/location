package com.example.location.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "access")
public class Access {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "lid", nullable = false)
    private Long lid;

    @Column(name = "type", nullable = false)
    private String type;

    public Access(Long uid, Long lid, String shareMode) {
        this.uid=uid;
        this.lid=lid;
        this.type = shareMode;
    }

    public Access() {
    }
    // constructors, getters and setters
}