package com.chatappbackend.backend.service.blocked;

import com.chatappbackend.backend.entity.BlockedUser;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.BlockedUserRepository;
import com.chatappbackend.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BlockedUserServiceImpl implements BlockedUserService{
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;

    public BlockedUserServiceImpl(UserRepository userRepository, BlockedUserRepository blockedUserRepository) {
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
    }

    @Override
    public void blockUser(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new RuntimeException("User not found"));

        if(blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser)){
            throw new RuntimeException("User is already blocked");
        }

        BlockedUser blockedUser = new BlockedUser();

        blockedUser.setBlockerId(user);
        blockedUser.setBlockedId(targetUser);
        blockedUser.setBlockedAt(LocalDateTime.now());

        blockedUserRepository.save(blockedUser);
    }

    @Override
    public void unblockUser(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new RuntimeException("User not found"));

        if(!blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser)){
            throw new RuntimeException("User is not blocked");
        }

        blockedUserRepository.deleteByBlockerIdAndBlockedId(user, targetUser);
    }

    @Override
    public boolean isBlocked(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new RuntimeException("User not found"));

        return blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser) || blockedUserRepository.existsByBlockerIdAndBlockedId(targetUser, user);
    }
}