package com.example.location.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserAccessDto {
    private String firstName;


    private String lastName;


    private String email;
    private String accessType;
}
