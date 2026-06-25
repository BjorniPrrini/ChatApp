package com.chatappbackend.backend.dto.auth;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String nickname;
    private String profilePicture;
}