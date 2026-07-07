package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.user.ChangePasswordRequestDTO;
import com.chatappbackend.backend.dto.user.UserRequestDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service){
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserById(){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(service.getUserById(currentUser.getId()));
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<UserResponseDTO> changeProfile(@RequestBody UserRequestDTO request){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserResponseDTO response = service.updateProfile(currentUser.getId(), request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDTO request){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword(), request.getConfirmPassword());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/searchUsers")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(@RequestParam String searchTerm){
        return ResponseEntity.ok(service.searchUsers(searchTerm));
    }

    @PutMapping("/profile-picture")
    public ResponseEntity<UserResponseDTO> updateProfilePicture(@RequestParam MultipartFile file){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(service.updateProfilePicture(currentUser.getId(), file));
    }
}