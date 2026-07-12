package com.chatappbackend.backend.dto.friend;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendResponseDTO {
    private Long senderId;
    private Long receiverId;
    private Long requestId;
    private String name;
    private String surname;
    private String nickname;
    private LocalDateTime createdAt;
    private String profilePicture;
    private String status;
}