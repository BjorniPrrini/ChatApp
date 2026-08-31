package com.chatappbackend.backend.dto.conversation;

import lombok.Data;

@Data
public class UpdateGroupRequestDTO {
    private String groupName;
    private String groupPicture;
}