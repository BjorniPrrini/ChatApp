package com.chatappfrontend.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ConversationResponseDTO {
    private Long conversationId;
    private String lastMessage;
    private List<ParticipantDTO> participants;
    @JsonProperty("lastMessageAt")
    private LocalDateTime lastMessageAt;
    @JsonProperty("group")
    private boolean isGroup;
    private String groupName;
    private String groupPicture;
    private boolean allowParticipantsInvite;
}