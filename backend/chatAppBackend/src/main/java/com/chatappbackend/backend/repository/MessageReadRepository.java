package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageRead;
import com.chatappbackend.backend.entity.MessageReadId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
    long countByMessage(Message message);
}