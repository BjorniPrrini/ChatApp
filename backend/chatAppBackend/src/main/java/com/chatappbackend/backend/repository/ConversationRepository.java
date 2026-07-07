package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT cp1.conversation FROM ConversationParticipant cp1 JOIN ConversationParticipant cp2 ON cp1.conversation = cp2.conversation WHERE cp1.user.id = :userId AND cp2.user.id = :receiverId AND cp1.conversation.isGroup = false")
    Optional<Conversation> findDMBetweenUsers(@Param("userId") Long userId, @Param("receiverId") Long receiverId);
    @Query("SELECT cp.conversation FROM ConversationParticipant cp WHERE cp.user.id = :userId AND cp.deletedAt IS NULL")
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);
}