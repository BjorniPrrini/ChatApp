package com.chatappbackend.backend.service.auth;

import com.chatappbackend.backend.dto.auth.AuthResponseDTO;
import com.chatappbackend.backend.dto.auth.LoginRequestDTO;
import com.chatappbackend.backend.dto.auth.RegisterRequestDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.UserRepository;
import com.chatappbackend.backend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestLogin) {
        User user = userRepository.findByEmail(requestLogin.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(requestLogin.getPassword(), user.getPasswordHash())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getId());

        return buildResponse(user, token);
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO requestRegister) {
        if(userRepository.existsByEmail(requestRegister.getEmail())){
            throw new RuntimeException("Email already in use");
        }

        if(!requestRegister.getPassword().equals(requestRegister.getConfirmPassword())){
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();

        user.setName(requestRegister.getName());
        user.setSurname(requestRegister.getSurname());
        user.setNickname(requestRegister.getNickname());
        user.setEmail(requestRegister.getEmail());
        user.setPasswordHash(passwordEncoder.encode(requestRegister.getPassword()));
        user.setPhoneNumber(requestRegister.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId());

        return buildResponse(user, token);
    }

    private AuthResponseDTO buildResponse(User user, String token){
        AuthResponseDTO response = new AuthResponseDTO();

        response.setToken(token);
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
}