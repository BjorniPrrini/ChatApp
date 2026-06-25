package com.chatappbackend.backend.service.user;

import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateProfile(Long id, UserRequestDTO request);
    void changePassword(Long id, String currentPassword, String newPassword, String confirmPassword);
    List<UserResponseDTO> searchUsers(String searchTerm);
}