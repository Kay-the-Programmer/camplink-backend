package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/// An FCM registration token for one of a user's devices. Used to deliver push
/// notifications. A token is globally unique and always points at the user who
/// most recently registered it.
@Entity
@Table(name = "device_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceToken {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
