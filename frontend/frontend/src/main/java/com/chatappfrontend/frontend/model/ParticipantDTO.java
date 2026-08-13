package com.chatappfrontend.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParticipantDTO {
    private Long userId;
    private String name;
    private String surname;
    private String nickname;
    private String profilePicture;
    @JsonProperty("online")
    private boolean isOnline;
}