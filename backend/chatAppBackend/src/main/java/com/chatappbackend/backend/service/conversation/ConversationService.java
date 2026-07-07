package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;

import java.util.List;

public interface ConversationService {
    ConversationResponseDTO createConversation(Long userId, ConversationRequestDTO request);
    List<ConversationResponseDTO> getUserConversations(Long userId);
    ConversationResponseDTO getConversationById(Long userId, Long conversationId);
    void deleteConversation(Long userId, Long conversationId);
}