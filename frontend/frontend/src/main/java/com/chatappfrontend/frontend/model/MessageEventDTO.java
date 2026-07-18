package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageEventDTO {
    private String type;
    private Long conversationId;
    private Long messageId;
    private MessageResponseDTO message;
}