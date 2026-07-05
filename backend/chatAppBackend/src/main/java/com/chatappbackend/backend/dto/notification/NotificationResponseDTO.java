package com.chatappbackend.backend.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}