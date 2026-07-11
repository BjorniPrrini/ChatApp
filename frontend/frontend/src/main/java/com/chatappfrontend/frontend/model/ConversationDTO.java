package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversationDTO {
    private Long conversationId;
    private Long otherUserId;
    private String name;
    private String surname;
    private String nickname;
    private String profilePicture;
    private String lastMessage;
    private boolean isGroup;
    private String groupName;
    private boolean isOnline;
}