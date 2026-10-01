package com.chatappbackend.backend.service.summary;

import com.chatappbackend.backend.dto.summary.SummaryResponseDTO;

public interface SummaryService {
    SummaryResponseDTO getSummary(Long userId, Long conversationId);
}