package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.auth.AuthResponseDTO;
import com.chatappbackend.backend.dto.auth.LoginRequestDTO;
import com.chatappbackend.backend.dto.auth.RegisterRequestDTO;
import com.chatappbackend.backend.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService authService){
        this.service = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request){
        AuthResponseDTO response = service.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request){
        AuthResponseDTO response = service.register(request);

        return ResponseEntity.ok(response);
    }
}