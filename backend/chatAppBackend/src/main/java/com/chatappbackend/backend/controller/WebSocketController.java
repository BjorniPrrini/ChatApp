package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.repository.ConversationParticipantRepository;
import com.chatappbackend.backend.repository.MessageRepository;
import com.chatappbackend.backend.service.message.MessageService;
import com.chatappbackend.backend.service.notification.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {
    private final MessageService service;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;

    public WebSocketController(MessageService service, SimpMessagingTemplate messagingTemplate, NotificationService notificationService, ConversationParticipantRepository conversationParticipantRepository, MessageRepository messageRepository) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(MessageRequestDTO request, Principal principal){
        Long userId = Long.parseLong(principal.getName());

        MessageResponseDTO message = service.sendMessage(userId, request);

        messagingTemplate.convertAndSend("/topic/conversation." + request.getConversationId(), message);

        conversationParticipantRepository.findOtherParticipant(request.getConversationId(), userId)
                .ifPresent(receiver -> {
                    notificationService.notifyUser(receiver.getId(), "NEW_MESSAGE", "New message from " + message.getSenderName(), message.getMessage());
                    messageRepository.markAsDelivered(message.getId());
                });
    }
}