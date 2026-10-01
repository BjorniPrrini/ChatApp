package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.ConversationSummary;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {

}