package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

}