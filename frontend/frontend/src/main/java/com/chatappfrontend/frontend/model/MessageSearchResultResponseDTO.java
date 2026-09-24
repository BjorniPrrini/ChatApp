package com.chatappfrontend.frontend.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageSearchResultResponseDTO {
    private Long messageId;
    private Long conversationId;
    private String senderName;
    private String senderSurname;
    private String message;
    private LocalDateTime sentAt;
}