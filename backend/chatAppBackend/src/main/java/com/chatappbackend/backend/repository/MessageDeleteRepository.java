package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageDelete;
import com.chatappbackend.backend.entity.MessageDeleteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageDeleteRepository extends JpaRepository<MessageDelete, MessageDeleteId> {
    long countByMessage(Message message);
}