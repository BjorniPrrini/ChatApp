package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversationMembershipEventDTO {
    String type;
    Long conversationId;
}