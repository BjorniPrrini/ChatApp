package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FriendResponseDTO {
    private Long senderId;
    private Long requestId;
    private String name;
    private String surname;
    private String nickname;
    private LocalDateTime createdAt;
    private String profilePicture;
    private String status;
}