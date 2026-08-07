package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.notification.NotificationResponseDTO;
import com.chatappbackend.backend.entity.Notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponseDTO toNotificationResponseDTO(Notification notification){
        NotificationResponseDTO notificationResponse = new NotificationResponseDTO();

        notificationResponse.setContent(notification.getContent());
        notificationResponse.setType(notification.getType());
        notificationResponse.setTitle(notification.getTitle());
        notificationResponse.setCreatedAt(notification.getCreatedAt());
        notificationResponse.setId(notification.getId());
        notificationResponse.setRead(notification.getIsRead());

        return notificationResponse;
    }
}