package com.chatappbackend.backend.dto.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingRequestDTO {
    private String model;
    private Content content;
    private Integer outputDimensionality;
}