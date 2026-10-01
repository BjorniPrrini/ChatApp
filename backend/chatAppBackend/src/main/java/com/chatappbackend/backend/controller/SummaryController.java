package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.summary.SummaryResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.summary.SummaryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {
    private final SummaryService service;

    public SummaryController(SummaryService service) {
        this.service = service;
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<SummaryResponseDTO> getSummary(@PathVariable("conversationId") Long conversationId){
        return ResponseEntity.ok(service.getSummary(getUser().getId(), conversationId));
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}