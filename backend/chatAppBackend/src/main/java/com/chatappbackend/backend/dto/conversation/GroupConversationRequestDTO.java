package com.chatappbackend.backend.dto.conversation;

import lombok.Data;

import java.util.List;

@Data
public class GroupConversationRequestDTO {
    private List<Long> participants;
    private String groupPicture;
    private String groupName;
}