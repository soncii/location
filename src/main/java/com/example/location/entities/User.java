package com.example.location.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long uid;

    private String firstName;

    private String lastName;

    private String email;
    private String password;
}
