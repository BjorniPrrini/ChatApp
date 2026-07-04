package com.chatappbackend.backend.service.auth;

import com.chatappbackend.backend.dto.auth.*;
import com.chatappbackend.backend.entity.PasswordResetToken;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.PasswordResetTokenRepository;
import com.chatappbackend.backend.repository.UserRepository;
import com.chatappbackend.backend.util.JwtUtil;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender javaMailSender;

    public AuthServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender javaMailSender){
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.javaMailSender = javaMailSender;
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

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not foud"));

        String randomDigit = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken(randomDigit);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        passwordResetToken.setUsed(false);

        passwordResetTokenRepository.save(passwordResetToken);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(user.getEmail());
        simpleMailMessage.setSubject("Password reset code");
        simpleMailMessage.setText("Your password reset code is: " + randomDigit + "\nExpires in 10 minutes.");

        javaMailSender.send(simpleMailMessage);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request){
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.getToken()).orElseThrow(() -> new RuntimeException("Token not found"));

        if(passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token has expired");
        }

        if(passwordResetToken.getUsed().equals(true)){
            throw new RuntimeException("Token has already been used before");
        }

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Confirm password is different from ney password");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        passwordResetToken.setUsed(true);

        passwordResetTokenRepository.save(passwordResetToken);
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