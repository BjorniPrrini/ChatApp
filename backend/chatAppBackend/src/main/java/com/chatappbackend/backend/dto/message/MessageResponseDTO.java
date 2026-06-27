package com.chatappbackend.backend.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderNickname;
    private String senderProfilePicture;
    private String message;
    private String status;
    private LocalDateTime sentAt;
    private Long replyToId;
    private String replyToMessage;
    private LocalDateTime editedAt;
    private boolean edited;
}