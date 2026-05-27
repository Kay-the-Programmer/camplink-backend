package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    private String phone;
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String photoUrl;
    private String hostel;
    private String location;

    @Column(nullable = false)
    private boolean suspended = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
