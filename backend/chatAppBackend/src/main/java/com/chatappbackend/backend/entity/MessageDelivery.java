package com.chatappbackend.backend.entity;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_deliveries")
@IdClass(MessageDeliveryId.class)
@Data
public class MessageDelivery {
    @Id
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}