package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sentAt < :before AND m.sentAt > :clearedAt ORDER BY m.sentAt DESC")
    List<Message> findMessages(@Param("conversationId") Long conversationId, @Param("before") LocalDateTime before, Pageable pageable, @Param("clearedAt") LocalDateTime clearedAt);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'read' WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'read'")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'delivered' WHERE m.id = :messageId AND m.status != 'read'")
    void markAsDelivered(@Param("messageId") Long messageId);
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);
    @Query("SELECT m.id FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'read'")
    List<Long> findUnreadMessageIds(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    @Query("SELECT m.id AS messageId, m.conversation.id AS conversationId, m.sender.id AS senderId FROM Message m WHERE m.conversation.id IN (SELECT cp.conversation.id FROM ConversationParticipant cp WHERE cp.user.id = :userId) AND m.status = 'sent' AND m.sender.id != :userId")
    List<UndeliveredMessageProjection> findUndeliveredMessages(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'delivered' WHERE m.id IN :messageIds")
    void markMessagesAsDelivered(@Param("messageIds") List<Long> messageIds);
}