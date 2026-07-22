package com.chatappbackend.backend.service.user;

import com.chatappbackend.backend.dto.auth.ChangePasswordRequestDTO;
import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ForbiddenException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setNickname(request.getNickname());
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);

        return mapToDTO(savedUser);
    }

    @Override
    public List<UserResponseDTO> searchUsers(String searchTerm, Long currentUserId) {
        List<User> users;

        if(searchTerm.contains("@")){
            users = userRepository.findByEmailContainingIgnoreCase(searchTerm);
        }else{
            users = userRepository.searchByNicknameOrName(searchTerm);
        }

        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Value("${file.upload-dir}")
    private String directoryName;

    @Override
    public UserResponseDTO updateProfilePicture(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String originalFilename = file.getOriginalFilename();

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9.]", "");

        String generatedName = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(directoryName + "avatars/").toAbsolutePath().normalize();

        try {
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(generatedName).normalize();

            if(!filePath.startsWith(uploadPath)){
                throw new ForbiddenException("Invalid file path");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new BadRequestException("Failed to save file");
        }

        user.setProfilePicture("uploads/avatars/" + generatedName);

        User savedUser = userRepository.save(user);

        return mapToDTO(savedUser);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO request){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())){
            throw new BadRequestException("Old password is wrong");
        }

        if(!request.getNewPassword().equals(request.getConfirmedPassword())){
            throw new BadRequestException("Confirm password does not mach new password");
        }

        if(request.getNewPassword().length() < 8){
            throw new BadRequestException("Password length must be 8 characters or more");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setProfilePicture(user.getProfilePicture());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setOnline(user.isOnline());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}