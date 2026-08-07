package com.chatappbackend.backend.service.user;

import com.chatappbackend.backend.dto.auth.ChangePasswordRequestDTO;
import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import com.chatappbackend.backend.dto.user.UserStatusEventDTO;
import com.chatappbackend.backend.entity.FriendRequest;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ForbiddenException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.mapper.UserMapper;
import com.chatappbackend.backend.repository.FriendRequestRepository;
import com.chatappbackend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final FriendRequestRepository friendRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, FriendRequestRepository friendRequestRepository, SimpMessagingTemplate messagingTemplate, UserMapper userMapper){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.friendRequestRepository = friendRequestRepository;
        this.messagingTemplate = messagingTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDTO getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(request.getName() != null && !request.getName().isBlank()){
            user.setName(request.getName());
        }

        if(request.getSurname() != null && !request.getSurname().isBlank()){
            user.setSurname(request.getSurname());
        }

        if(request.getNickname() != null){
            user.setNickname(request.getNickname());
        }

        if(request.getPhoneNumber() != null){
            if(request.getPhoneNumber().isBlank()){
                user.setPhoneNumber(null);
            }else{
                user.setPhoneNumber(request.getPhoneNumber());
            }
        }

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponseDTO(savedUser);
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
                .map(userMapper::toUserResponseDTO)
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

        return userMapper.toUserResponseDTO(savedUser);
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

    @Override
    public void setOfflineUser(Long userId){
        userRepository.updateUserStatusOffline(userId);

        broadcastStatusToFriends(userId, "offline");
    }

    @Override
    public void setOnlineUser(Long userId){
        userRepository.updateUserStatusOnline(userId);

        broadcastStatusToFriends(userId, "online");
    }

    private void broadcastStatusToFriends(Long userId, String status) {
        List<FriendRequest> friendsList = friendRequestRepository.findAcceptedFriendships(userId);

        List<Long> friendsId = friendsList.stream()
                .map(fr -> fr.getSender().getId().equals(userId) ? fr.getReceiver().getId() : fr.getSender().getId())
                .toList();

        UserStatusEventDTO event = new UserStatusEventDTO(userId, status);

        for(Long friendId : friendsId){
            messagingTemplate.convertAndSend("/queue/user." + friendId + ".status", event);
        }
    }
}