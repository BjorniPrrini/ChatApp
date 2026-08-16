package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageDelivery;
import com.chatappbackend.backend.entity.MessageDeliveryId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageDeliveryRepository extends JpaRepository<MessageDelivery, MessageDeliveryId> {
    long countByMessage(Message message);
}