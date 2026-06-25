package com.chatappbackend.backend.dto.user;

import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}