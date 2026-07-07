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

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sentAt < :before ORDER BY m.sentAt DESC")
    List<Message> findMessages(@Param("conversationId") Long conversationId, @Param("before") LocalDateTime before, Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'read' WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'read'")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'delivered' WHERE m.id = :messageId")
    void markAsDelivered(@Param("messageId") Long messageId);
}