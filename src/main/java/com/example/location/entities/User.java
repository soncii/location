package com.example.location.entities;

import lombok.Data;

import javax.persistence.*;

@Data
public class User {

    private Long uid;

    private String firstName;

    private String lastName;

    private String email;
    private String password;
}
