package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupConversationRequestDTO {
    private List<Long> participants;
    private String groupPicture;
    private String groupName;
}