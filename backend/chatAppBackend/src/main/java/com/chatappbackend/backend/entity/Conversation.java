package com.chatappbackend.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "is_group")
    private Boolean isGroup = false;

    @Column(name = "group_picture")
    private String groupPicture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}