package com.chatappbackend.backend.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class ConversationParticipantId implements Serializable {
    private Long conversation;
    private Long user;
}