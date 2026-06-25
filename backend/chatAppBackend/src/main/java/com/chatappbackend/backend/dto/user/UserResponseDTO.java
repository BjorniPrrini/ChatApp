package com.chatappbackend.backend.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String profilePicture;
    private String phoneNumber;
    private boolean isOnline;
    private LocalDateTime createdAt;
}