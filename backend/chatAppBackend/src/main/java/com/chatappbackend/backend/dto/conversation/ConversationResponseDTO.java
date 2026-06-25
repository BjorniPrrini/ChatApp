package com.chatappbackend.backend.dto.conversation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationResponseDTO {
    private Long conversationId;
    private Long otherUserId;
    private String name;
    private String surname;
    private String nickname;
    private String profilePicture;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean isGroup;
    private String groupName;
    private String groupPicture;
    private boolean isOnline;
}