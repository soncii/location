package com.example.location.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "friendship")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid_1", nullable = false)
    private Long uid1;

    @Column(name = "uid_2", nullable = false)
    private Long uid2;

    // constructors, getters and setters
}