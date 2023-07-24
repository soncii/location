package com.example.location.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {

    private Long hid;
    private Long actionBy;
    private String objectType;
    private String action;
    private String actionDetails;
    private Timestamp date;
}
