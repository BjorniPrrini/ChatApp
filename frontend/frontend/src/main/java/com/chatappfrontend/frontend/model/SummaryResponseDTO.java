package com.chatappfrontend.frontend.model;

import lombok.Data;

@Data
public class SummaryResponseDTO {
    private Long conversationId;
    private String summary;
    private int remainingQuota;
}