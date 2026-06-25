package com.chatappbackend.backend.service.user;

import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setNickname(request.getNickname());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return mapToDTO(savedUser);
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if(!newPassword.equals(confirmPassword)){
            throw new RuntimeException("New password does not mach confirm password");
        }

        if(!passwordEncoder.matches(currentPassword, user.getPasswordHash())){
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    public List<UserResponseDTO> searchUsers(String searchTerm) {
        List<User> users;

        if(searchTerm.contains("@")){
            users = userRepository.findByEmailContainingIgnoreCase(searchTerm);
        }else{
            users = userRepository.findByNicknameContainingIgnoreCase(searchTerm);
        }

        return users.stream()
                .map(user -> mapToDTO(user))
                .collect(Collectors.toList());
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