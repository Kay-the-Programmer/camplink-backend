package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shopping_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShoppingRequest {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    private String title;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "request_id")
    private List<ShoppingRequestItem> items;

    @Column(nullable = false)
    private String deliveryHostel;

    private String deliveryRoom;

    private BigDecimal budget;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id")
    private User runner;

    private BigDecimal runnerFee;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
