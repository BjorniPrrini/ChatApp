package com.chatappbackend.backend.dto.conversation;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationResponseDTO {
    private Long conversationId;
    private boolean isGroup;
    private String groupName;
    private String groupPicture;
    private List<ParticipantDTO> participants;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}