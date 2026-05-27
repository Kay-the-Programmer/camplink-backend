package com.camplink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {

    /** Deterministic ID built from sorted participant UIDs: uid1_uid2 */
    @Id
    @Column(length = 80)
    private String id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_sender_id")
    private User lastSender;

    private LocalDateTime updatedAt;
}
