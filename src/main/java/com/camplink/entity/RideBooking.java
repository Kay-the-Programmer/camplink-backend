package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/// A campus ride request placed by a passenger and (optionally) accepted by a
/// driver. Mirrors the Flutter RideBooking model.
@Entity
@Table(name = "ride_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RideBooking {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // "from"/"to" are SQL keywords — store under safe column names.
    @Column(name = "from_location", nullable = false)
    private String fromLocation;

    @Column(name = "to_location", nullable = false)
    private String toLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "drop_off", nullable = false)
    private DropOff dropOff;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_type", nullable = false)
    private RideType type;

    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private int seats;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_status", nullable = false)
    private RideStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Column(precision = 10, scale = 2)
    private BigDecimal fare;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
