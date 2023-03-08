package com.example.location.entities;

import com.example.location.dto.UserAccessDto;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "access")
@NamedNativeQuery(name="getUserAccessByLocationId",query = "SELECT u.firstName AS firstName, u.lastName AS lastName, u.email AS email, a.type AS accessType " +
        "FROM Access a " +
        "JOIN Users u ON u.uid = a.uid " +
        "WHERE a.lid = :lid", resultSetMapping = "locationMap")
@SqlResultSetMapping(name="locationMap", classes = @ConstructorResult(targetClass = UserAccessDto.class, columns = {
        @ColumnResult(name="firstName", type=String.class),
        @ColumnResult(name="lastName", type=String.class),
        @ColumnResult(name="email", type=String.class),
        @ColumnResult(name="accessType", type=String.class)
}))
public class Access {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "lid", nullable = false)
    private Long lid;

    @Column(name = "type", nullable = false)
    private String type;

    // constructors, getters and setters
}