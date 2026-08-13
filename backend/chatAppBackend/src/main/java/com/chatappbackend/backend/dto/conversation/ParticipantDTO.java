package com.chatappbackend.backend.dto.conversation;

import lombok.Data;

@Data
public class ParticipantDTO {
    private Long userId;
    private String name;
    private String surname;
    private String nickname;
    private String profilePicture;
    private boolean isOnline;
}