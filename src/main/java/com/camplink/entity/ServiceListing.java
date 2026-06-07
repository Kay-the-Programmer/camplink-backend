package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/// A student-offered service (tutoring, laundry, haircut, …). Mirrors the
/// Flutter ServiceListing model. Category is stored verbatim as the Flutter
/// enum name (e.g. "techHelp") so the round-trip is exact.
@Entity
@Table(name = "service_listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceListing {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 200)
    private String priceNote;

    @Column(nullable = false)
    private boolean available;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
