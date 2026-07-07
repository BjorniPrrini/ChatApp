package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.BlockedUser;
import com.chatappbackend.backend.entity.BlockedUserId;
import com.chatappbackend.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, BlockedUserId> {
    boolean existsByBlockerIdAndBlockedId(User blockerId, User blockedId);
    @Modifying
    @Transactional
    void deleteByBlockerIdAndBlockedId(User blockerId, User blockedId);
}