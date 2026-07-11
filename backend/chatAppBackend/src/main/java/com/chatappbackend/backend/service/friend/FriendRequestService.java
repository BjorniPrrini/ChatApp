package com.chatappbackend.backend.service.friend;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;

import java.util.List;

public interface FriendRequestService {
    void sendFriendRequest(Long userId, Long receiverId);
    void acceptFriendRequest(Long userId, Long senderId);
    void rejectFriendRequest(Long userId, Long senderId);
    List<FriendResponseDTO> getFriendRequests(Long userId);
    List<FriendResponseDTO> getFriends(Long userId);
    List<FriendResponseDTO> getSuggestedFriends(Long userId);
}