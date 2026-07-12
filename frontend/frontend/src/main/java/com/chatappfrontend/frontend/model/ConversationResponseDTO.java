package com.chatappfrontend.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ConversationResponseDTO {
    private Long conversationId;
    private Long otherUserId;
    private String name;
    private String surname;
    private String nickname;
    private String profilePicture;
    private String lastMessage;
    @JsonProperty("lastMessageAt")
    private LocalDateTime lastMessageAt;
    @JsonProperty("group")
    private boolean isGroup;
    private String groupName;
    private String groupPicture;
    @JsonProperty("online")
    private boolean isOnline;
}