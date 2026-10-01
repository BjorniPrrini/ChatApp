package com.chatappbackend.backend.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponseDTO {
    private Long conversationId;
    private String summary;
    private int remainingQuota;
}