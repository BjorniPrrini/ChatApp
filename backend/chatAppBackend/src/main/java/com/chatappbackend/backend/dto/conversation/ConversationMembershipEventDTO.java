package com.chatappbackend.backend.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMembershipEventDTO {
    private String type;
    private Long conversationId;
}