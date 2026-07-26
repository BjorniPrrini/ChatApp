package com.chatappbackend.backend.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventDTO {
    private String type;
    private Long conversationId;
    private Long messageId;
    private MessageResponseDTO message;
    private List<Long> messageIds;
}