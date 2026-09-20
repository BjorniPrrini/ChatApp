package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sentAt < :before AND m.sentAt > :clearedAt AND NOT EXISTS (SELECT 1 FROM MessageDelete md WHERE md.message = m AND md.user.id = :userId) ORDER BY m.sentAt DESC")
    List<Message> findMessages(@Param("userId") Long userId, @Param("conversationId") Long conversationId, @Param("before") LocalDateTime before, Pageable pageable, @Param("clearedAt") LocalDateTime clearedAt);
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);
    @Query("SELECT m.id FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'read'")
    List<Long> findUnreadMessageIds(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    @Query("SELECT m.id AS messageId, m.conversation.id AS conversationId, m.sender.id AS senderId FROM Message m WHERE m.conversation.id IN (SELECT cp.conversation.id FROM ConversationParticipant cp WHERE cp.user.id = :userId) AND m.status = 'sent' AND m.sender.id != :userId")
    List<UndeliveredMessageProjection> findUndeliveredMessages(@Param("userId") Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Message m WHERE m.id = :messageId")
    Optional<Message> findByIdForUpdate(@Param("messageId") Long messageId);
    @Query("SELECT m FROM Message m LEFT JOIN MessageEmbedding me ON me.messageId = m.id WHERE me IS NULL")
    List<Message> findMessageWithNoEmbedding();
}