package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String nickname;
    private String profilePicture;
}