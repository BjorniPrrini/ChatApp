package com.chatappbackend.backend.service.message;

import com.chatappbackend.backend.dto.message.MessagePageDTO;
import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;

import java.time.LocalDateTime;

public interface MessageService {
    MessageResponseDTO sendMessage(Long userId, MessageRequestDTO request);
    MessagePageDTO getMessages(Long userId, Long conversationId, LocalDateTime before);
    void deleteMessageForMe(Long userId, Long messageId);
    void deleteMessageForEveryone(Long userId, Long messageId);
    MessageResponseDTO editMessage(Long userId, Long messageId, String newMessage);
}