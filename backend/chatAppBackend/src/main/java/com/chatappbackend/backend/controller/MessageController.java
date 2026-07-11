package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.message.MessagePageDTO;
import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.message.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService messageService) {
        this.service = messageService;
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<MessageResponseDTO> sendMessage(@RequestBody MessageRequestDTO request){
        return ResponseEntity.ok(service.sendMessage(getUser().getId(), request));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<MessagePageDTO> getMessages(@PathVariable Long conversationId, @RequestParam(required = false) LocalDateTime before){
        if(before == null){
            before = LocalDateTime.now();
        }

        return ResponseEntity.ok(service.getMessages(getUser().getId(), conversationId, before));
    }

    @DeleteMapping("/{messageId}/me")
    public ResponseEntity<Void> deleteMessageForMe(@PathVariable Long messageId){
        service.deleteMessageForMe(getUser().getId(), messageId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{messageId}/everyone")
    public ResponseEntity<Void> deleteMessageForEveryone(@PathVariable Long messageId){
        service.deleteMessageForEveryone(getUser().getId(), messageId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponseDTO> editMessage(@PathVariable Long messageId, @RequestParam String newMessage){
        return ResponseEntity.ok(service.editMessage(getUser().getId(), messageId, newMessage));
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}