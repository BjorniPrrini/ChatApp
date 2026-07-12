package com.chatappfrontend.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("online")
    private boolean isOnline;
    private String createdAt;
}