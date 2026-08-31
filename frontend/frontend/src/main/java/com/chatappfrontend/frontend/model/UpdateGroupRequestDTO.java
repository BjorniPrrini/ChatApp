package com.chatappfrontend.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequestDTO {
    private String groupName;
    private String groupPicture;
}