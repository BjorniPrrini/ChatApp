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

@RestController
@RequestMapping("/api/message")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<MessageResponseDTO> sendMessage(@RequestBody MessageRequestDTO request){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(messageService.sendMessage(currentUser.getId(), request));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<MessagePageDTO> getMessages(@PathVariable Long conversationId, @RequestParam(required = false) LocalDateTime before){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(before == null){
            before = LocalDateTime.now();
        }

        return ResponseEntity.ok(messageService.getMessages(currentUser.getId(), conversationId, before));
    }

    @DeleteMapping("/{messageId}/me")
    public ResponseEntity<Void> deleteMessageForMe(@PathVariable Long messageId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        messageService.deleteMessageForMe(currentUser.getId(), messageId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{messageId}/everyone")
    public ResponseEntity<Void> deleteMessageForEveryone(@PathVariable Long messageId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        messageService.deleteMessageForEveryone(currentUser.getId(), messageId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponseDTO> editMessage(@PathVariable Long messageId, @RequestParam String newMessage){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(messageService.editMessage(currentUser.getId(), messageId, newMessage));
    }
}