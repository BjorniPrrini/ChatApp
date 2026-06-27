package com.chatappbackend.backend.dto.message;

import lombok.Data;

import java.util.List;

@Data
public class MessagePageDTO {
    private List<MessageResponseDTO> messages;
    private boolean hasMore;
}