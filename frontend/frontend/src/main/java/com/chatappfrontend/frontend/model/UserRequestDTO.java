package com.chatappfrontend.frontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequestDTO {
    private String name;
    private String surname;
    private String nickname;
    private String phoneNumber;
}