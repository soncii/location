package com.example.location.dto;

import lombok.Data;

@Data
public class UserAccessDto {
    private String firstName;


    private String lastName;


    private String email;
    private String accessType;

    public UserAccessDto() {
    }

    public UserAccessDto(String firstName, String lastName, String email, String accessType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accessType = accessType;
    }
}
