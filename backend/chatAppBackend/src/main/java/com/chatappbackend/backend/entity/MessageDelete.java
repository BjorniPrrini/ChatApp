package com.chatappbackend.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_deletes")
@IdClass(MessageDeleteId.class)
@Data
public class MessageDelete {

    @Id
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}