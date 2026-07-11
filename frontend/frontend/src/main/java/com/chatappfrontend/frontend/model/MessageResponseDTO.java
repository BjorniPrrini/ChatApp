package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderNickname;
    private String senderProfilePicture;
    private String message;
    private String status;
    private LocalDateTime sentAt;
    private Long replyToId;
    private String replyToMessage;
    private LocalDateTime editedAt;
    private boolean edited;
}