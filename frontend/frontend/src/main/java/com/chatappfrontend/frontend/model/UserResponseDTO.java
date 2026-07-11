package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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