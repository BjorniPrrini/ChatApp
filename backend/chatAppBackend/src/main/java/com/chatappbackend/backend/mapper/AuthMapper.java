package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.auth.AuthResponseDTO;
import com.chatappbackend.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponseDTO toAuthResponseDTO(User user, String token){
        AuthResponseDTO response = new AuthResponseDTO();

        response.setToken(token);
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
}