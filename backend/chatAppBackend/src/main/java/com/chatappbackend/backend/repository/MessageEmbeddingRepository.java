package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.MessageEmbedding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageEmbeddingRepository extends JpaRepository<MessageEmbedding, Long> {
    @Query(value = "SELECT me.message_id FROM message_embedding me JOIN messages m ON m.id = me.message_id WHERE EXISTS (SELECT 1 FROM conversation_participants cp WHERE cp.conversation_id = m.conversation_id AND cp.user_id = :userId) ORDER BY me.embedding <=> CAST(:queryVector AS vector) LIMIT :limit", nativeQuery = true)
    List<Long> searchMessageByVector(@Param("userId") Long userId, @Param("queryVector") String queryVector, @Param("limit") int limit);
}