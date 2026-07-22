package com.chatappbackend.backend.service.user;

import com.chatappbackend.backend.dto.auth.ChangePasswordRequestDTO;
import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateProfile(Long id, UserRequestDTO request);
    List<UserResponseDTO> searchUsers(String searchTerm, Long currentUserId);
    UserResponseDTO updateProfilePicture(Long userId, MultipartFile file);
    void changePassword(Long userId, ChangePasswordRequestDTO request);
}