package com.chatappbackend.backend.dto.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingResponseDTO {
    private Embedding embedding;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Embedding {
        private List<Float> values;
    }
}