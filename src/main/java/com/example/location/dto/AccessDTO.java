package com.example.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessDTO {

    private Long lid;
    private String email;
    private String shareMode;
}
