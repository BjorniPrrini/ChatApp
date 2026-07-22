package com.chatappbackend.backend.dto.auth;

import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmedPassword;
}