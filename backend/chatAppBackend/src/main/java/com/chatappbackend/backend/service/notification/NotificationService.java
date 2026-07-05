package com.chatappbackend.backend.service.notification;

import com.chatappbackend.backend.dto.notification.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    void notifyUser(Long userId, String type, String title, String content);
    List<NotificationResponseDTO> getUserNotifications(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
}