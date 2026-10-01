package com.chatappbackend.backend.dto.summary;

import com.chatappbackend.backend.dto.embedding.Content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiSummarizeRequestDTO {
    private SystemInstruction systemInstruction;
    private List<Content> contents;
}