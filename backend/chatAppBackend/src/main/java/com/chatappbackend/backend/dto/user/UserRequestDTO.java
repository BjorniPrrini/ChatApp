package com.chatappbackend.backend.dto.user;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String surname;
    private String nickname;
    private String phoneNumber;
}