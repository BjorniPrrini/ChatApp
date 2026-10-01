package com.chatappbackend.backend.entity;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "conversation_summaries")
@Data
public class ConversationSummary {
    @Id
    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "last_message_id")
    private Long lastMessageId;
}