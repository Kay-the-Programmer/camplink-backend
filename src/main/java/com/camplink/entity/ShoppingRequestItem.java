package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shopping_request_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShoppingRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    private BigDecimal estimatedPrice;

    private String notes;
}
