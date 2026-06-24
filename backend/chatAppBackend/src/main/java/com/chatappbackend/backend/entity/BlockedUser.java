package com.chatappbackend.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_users")
@IdClass(BlockedUserId.class)
@Data
public class BlockedUser {
    @Id
    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blockerId;

    @Id
    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blockedId;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
}