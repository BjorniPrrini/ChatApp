package com.chatappbackend.backend.service.auth;

import com.chatappbackend.backend.dto.auth.*;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO requestLogin);
    AuthResponseDTO register(RegisterRequestDTO requestRegister);
    void forgotPassword(ForgotPasswordRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
}