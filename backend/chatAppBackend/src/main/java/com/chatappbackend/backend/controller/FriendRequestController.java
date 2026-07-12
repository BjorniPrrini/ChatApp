package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.friend.FriendRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendResponseDTO>> getSentRequests(){
        return ResponseEntity.ok(friendRequestService.getSentRequests(getUser().getId()));
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Void> sendFriendRequest(@PathVariable Long receiverId){
        friendRequestService.sendFriendRequest(getUser().getId(), receiverId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/accept/{senderId}")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long senderId){
        friendRequestService.acceptFriendRequest(getUser().getId(), senderId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/reject/{senderId}")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long senderId){
        friendRequestService.rejectFriendRequest(getUser().getId(), senderId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponseDTO>> getRequests(){
        return ResponseEntity.ok(friendRequestService.getFriendRequests(getUser().getId()));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendResponseDTO>> getFriends(){
        return ResponseEntity.ok(friendRequestService.getFriends(getUser().getId()));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<FriendResponseDTO>> getSuggestions(){
        return ResponseEntity.ok(friendRequestService.getSuggestedFriends(getUser().getId()));
    }

    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendId){
        friendRequestService.removeFriend(getUser().getId(), friendId);

        return ResponseEntity.ok().build();
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}