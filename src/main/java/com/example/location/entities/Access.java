package com.example.location.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Access {

    private Long aid;

    private Long uid;

    private Long lid;
    private String type;
}