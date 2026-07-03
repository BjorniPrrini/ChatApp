package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.service.message.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {
    private final MessageService service;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(MessageService service, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(MessageRequestDTO request, Principal principal){
        Long userId = Long.parseLong(principal.getName());

        MessageResponseDTO message = service.sendMessage(userId, request);

        messagingTemplate.convertAndSend("/topic/conversation." + request.getConversationId(), message);
    }
}