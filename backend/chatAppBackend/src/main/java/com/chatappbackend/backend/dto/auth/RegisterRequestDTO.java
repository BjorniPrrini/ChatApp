package com.chatappbackend.backend.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
}