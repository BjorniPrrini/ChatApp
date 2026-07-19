package com.chatappbackend.backend.service.blocked;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;

import java.util.List;

public interface BlockedUserService {
    void blockUser(Long userId, Long otherUserId);
    void unblockUser(Long userId, Long otherUserId);
    boolean isBlocked(Long userId, Long otherUserId);
    List<FriendResponseDTO> getBlockedUsers(Long userId);
}