package com.chatappbackend.backend.service.notification;

import com.chatappbackend.backend.dto.notification.NotificationResponseDTO;
import com.chatappbackend.backend.entity.Notification;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.NotificationRepository;
import com.chatappbackend.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService{
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(UserRepository userRepository, NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyUser(Long userId, String type, String title, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setContent(content);
        notification.setType(type);
        notification.setTitle(title);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notificationList = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notificationList.stream()
                .map(notification -> mapToDTO(notification))
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found"));

        if(!notification.getUser().equals(user)){
            throw new RuntimeException("This notification does not belong to the current user");
        }

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationResponseDTO mapToDTO(Notification notification){
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