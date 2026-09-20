package com.chatappbackend.backend.service.embedding;

import com.chatappbackend.backend.dto.message.MessageSearchResultDTO;
import com.chatappbackend.backend.exception.EmbeddingException;

import java.util.List;

public interface EmbeddingService {
    float[] messageToVector(String message) throws EmbeddingException;
    List<MessageSearchResultDTO> searchMessages(Long userId, String query, int limit) throws EmbeddingException;
}