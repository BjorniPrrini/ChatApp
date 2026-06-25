package com.chatappbackend.backend.service.auth;

import com.chatappbackend.backend.dto.auth.AuthResponseDTO;
import com.chatappbackend.backend.dto.auth.LoginRequestDTO;
import com.chatappbackend.backend.dto.auth.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO requestLogin);
    AuthResponseDTO register(RegisterRequestDTO requestRegister);
}