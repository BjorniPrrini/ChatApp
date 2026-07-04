package com.chatappbackend.backend.dto.auth;

import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    private String email;
}