package com.chatappfrontend.frontend.model;

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