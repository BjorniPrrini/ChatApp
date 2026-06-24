package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.ConversationParticipant;
import com.chatappbackend.backend.entity.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

}