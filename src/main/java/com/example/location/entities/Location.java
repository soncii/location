package com.example.location.entities;

import com.example.location.dto.SharedLocation;
import com.example.location.dto.UserAccessDto;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "location")
@NamedNativeQuery(name="AllLocations",query = "Select l.lid as lid, u.email as email,l.name as name,l.address as address, " +
        "a.type as accessType from Location l " +
        "Join Access a on l.lid=a.lid join users u on u.uid=l.uid WHERE a.uid=:uid"
        , resultSetMapping = "sharedLocationMap")
@SqlResultSetMapping(name="sharedLocationMap", classes = @ConstructorResult(targetClass = SharedLocation.class, columns = {
        @ColumnResult(name="lid", type=Long.class),
        @ColumnResult(name="email", type=String.class),
        @ColumnResult(name="name", type=String.class),
        @ColumnResult(name="address", type=String.class),
        @ColumnResult(name="accessType", type=String.class)
}))
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lid;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    // constructors, getters and setters
}