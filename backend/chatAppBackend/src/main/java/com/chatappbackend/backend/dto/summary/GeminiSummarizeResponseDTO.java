package com.chatappbackend.backend.dto.summary;

import lombok.Data;

import java.util.List;

@Data
public class GeminiSummarizeResponseDTO {
    private List<Candidate> candidates;
}