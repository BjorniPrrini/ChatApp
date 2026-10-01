package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.summary.SummaryResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class SummaryMapper {
    public SummaryResponseDTO toSummaryResponseDTO(Long conversationId, String summary, int remainingQuota){
        SummaryResponseDTO response = new SummaryResponseDTO();

        response.setConversationId(conversationId);
        response.setSummary(summary);
        response.setRemainingQuota(remainingQuota);

        return response;
    }
}