package com.chatappbackend.backend.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageSearchResultDTO {
    private Long messageId;
    private Long conversationId;
    private String senderName;
    private String senderProfilePicture;
    private String message;
    private LocalDateTime sentAt;
}