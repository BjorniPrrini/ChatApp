package com.chatappbackend.backend.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String email;
    private String token;
    private String newPassword;
    private String confirmPassword;
}