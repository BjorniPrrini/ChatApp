package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.conversation.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    private final ConversationService service;

    public ConversationController(ConversationService service){
        this.service = service;
    }

    @PostMapping("/createConversation")
    public ResponseEntity<ConversationResponseDTO> createConversation(@RequestBody ConversationRequestDTO request){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConversationResponseDTO response = service.createConversation(currentUser.getId(), request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserConversations")
    public ResponseEntity<List<ConversationResponseDTO>> getUserConversations(){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(service.getUserConversations(currentUser.getId()));
    }

    @GetMapping("/{conversationId}")
    public  ResponseEntity<ConversationResponseDTO> getConversationById(@PathVariable Long conversationId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(service.getConversationById(currentUser.getId(), conversationId));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.deleteConversation(currentUser.getId(), conversationId);

        return ResponseEntity.ok().build();
    }
}