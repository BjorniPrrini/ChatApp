package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.conversation.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    private final ConversationService service;

    public ConversationController(ConversationService service){
        this.service = service;
    }

    @PostMapping("/createConversation")
    public ResponseEntity<ConversationResponseDTO> createConversation(@RequestBody ConversationRequestDTO request){
        ConversationResponseDTO response = service.createConversation(getUser().getId(), request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserConversations")
    public ResponseEntity<List<ConversationResponseDTO>> getUserConversations(){
        return ResponseEntity.ok(service.getUserConversations(getUser().getId()));
    }

    @GetMapping("/{conversationId}")
    public  ResponseEntity<ConversationResponseDTO> getConversationById(@PathVariable Long conversationId){
        return ResponseEntity.ok(service.getConversationById(getUser().getId(), conversationId));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId){
        service.deleteConversation(getUser().getId(), conversationId);

        return ResponseEntity.ok().build();
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}