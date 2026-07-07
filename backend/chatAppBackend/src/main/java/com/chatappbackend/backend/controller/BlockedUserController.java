package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.blocked.BlockedUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/block")
public class BlockedUserController {
    private final BlockedUserService service;

    public BlockedUserController(BlockedUserService service) {
        this.service = service;
    }

    @PostMapping("/{otherUserId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long otherUserId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.blockUser(currentUser.getId(), otherUserId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{otherUserId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long otherUserId){
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        service.unblockUser(currentUser.getId(), otherUserId);

        return ResponseEntity.ok().build();
    }
}