package com.chatappbackend.backend.repository;

public interface UndeliveredMessageProjection {
    Long getMessageId();
    Long getConversationId();
    Long getSenderId();
}