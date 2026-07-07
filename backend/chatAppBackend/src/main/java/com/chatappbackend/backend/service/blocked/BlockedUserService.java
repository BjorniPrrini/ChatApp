package com.chatappbackend.backend.service.blocked;

public interface BlockedUserService {
    void blockUser(Long userId, Long otherUserId);
    void unblockUser(Long userId, Long otherUserId);
    boolean isBlocked(Long userId, Long otherUserId);
}