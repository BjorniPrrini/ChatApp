package com.chatappbackend.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}