package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sentAt < :before ORDER BY m.sentAt DESC")
    List<Message> findMessages(@Param("conversationId") Long conversationId, @Param("before") LocalDateTime before, Pageable pageable);
}