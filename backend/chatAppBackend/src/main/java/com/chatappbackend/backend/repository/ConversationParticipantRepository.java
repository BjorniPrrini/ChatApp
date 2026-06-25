package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.ConversationParticipant;
import com.chatappbackend.backend.entity.ConversationParticipantId;
import com.chatappbackend.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {
    @Query("SELECT cp.user FROM ConversationParticipant cp WHERE cp.conversation.id = :conversationId AND cp.user.id != :userId")
    Optional<User> findOtherParticipant(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}