package com.chatappbackend.backend.dto.message;

import lombok.Data;

@Data
public class MessageRequestDTO {
    private Long conversationId;
    private String message;
    private Long replyToId;
}