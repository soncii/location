package com.example.location.entities;

import com.example.location.util.Util;
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

    @Override
    public String toString() {

        return "User{" +
            "uid=" + uid +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + Util.hideEmail(email) + '\'' +
            '}';
    }
}
