package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.blocked.BlockedUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/block")
public class BlockedUserController {
    private final BlockedUserService service;

    public BlockedUserController(BlockedUserService service) {
        this.service = service;
    }

    @PostMapping("/{otherUserId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long otherUserId){
        service.blockUser(getUser().getId(), otherUserId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{otherUserId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long otherUserId){
        service.unblockUser(getUser().getId(), otherUserId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/getBlocked")
    public ResponseEntity<List<FriendResponseDTO>> getBlockedUsers(){
        return ResponseEntity.ok(service.getBlockedUsers(getUser().getId()));
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}