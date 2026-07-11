package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.notification.NotificationResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(){
        return ResponseEntity.ok(service.getUserNotifications(getUser().getId()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> maskAsRead(@PathVariable Long notificationId){
        service.markAsRead(getUser().getId(), notificationId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> maskAllAsRead(){
        service.markAllAsRead(getUser().getId());

        return ResponseEntity.ok().build();
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}