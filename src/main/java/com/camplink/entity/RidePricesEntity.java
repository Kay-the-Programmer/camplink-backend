package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;

/// Per-seat ride fares configured by the admin. Stored as a single row keyed by
/// {@link #SINGLETON_ID}; the students' booking screen and the admin "Ride
/// Settings" tab both read and write this.
@Entity
@Table(name = "ride_prices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RidePricesEntity {

    public static final String SINGLETON_ID = "DEFAULT";

    @Id
    @Column(length = 16)
    private String id;

    @Column(nullable = false)
    private double campusToTown;

    @Column(nullable = false)
    private double townToAcross;

    @Column(nullable = false)
    private double townToInsideCampus;
}
