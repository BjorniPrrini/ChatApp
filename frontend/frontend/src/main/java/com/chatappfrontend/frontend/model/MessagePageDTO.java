package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MessagePageDTO {
    private List<MessageResponseDTO> messages;
    private boolean hasMore;
}