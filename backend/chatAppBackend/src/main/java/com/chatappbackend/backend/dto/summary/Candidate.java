package com.chatappbackend.backend.dto.summary;

import com.chatappbackend.backend.dto.embedding.Content;

import lombok.Data;

@Data
public class Candidate {
    private Content content;
}