package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.message.MessageSearchResultDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.EmbeddingException;
import com.chatappbackend.backend.service.embedding.EmbeddingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {
    private final EmbeddingService service;

    public EmbeddingController(EmbeddingService service) {
        this.service = service;
    }

    @GetMapping("/messageSearchResult/{query}")
    public ResponseEntity<List<MessageSearchResultDTO>> messageSearchResult(@RequestParam String query) throws EmbeddingException {
        return ResponseEntity.ok(service.searchMessages(getUser().getId(), query, 20));
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}