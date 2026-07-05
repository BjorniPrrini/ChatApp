package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.notification.NotificationResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(service.getUserNotifications(currentUser.getId()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> maskAsRead(@PathVariable Long notificationId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.markAsRead(currentUser.getId(), notificationId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> maskAllAsRead(){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.markAllAsRead(currentUser.getId());

        return ResponseEntity.ok().build();
    }
}