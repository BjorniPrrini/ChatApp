package com.chatappbackend.backend.service.blocked;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.BlockedUser;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.mapper.UserMapper;
import com.chatappbackend.backend.repository.BlockedUserRepository;
import com.chatappbackend.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockedUserServiceImpl implements BlockedUserService{
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserMapper userMapper;

    public BlockedUserServiceImpl(UserRepository userRepository, BlockedUserRepository blockedUserRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public void blockUser(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser)){
            throw new BadRequestException("User is already blocked");
        }

        BlockedUser blockedUser = new BlockedUser();

        blockedUser.setBlockerId(user);
        blockedUser.setBlockedId(targetUser);
        blockedUser.setBlockedAt(LocalDateTime.now());

        blockedUserRepository.save(blockedUser);
    }

    @Override
    public void unblockUser(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser)){
            throw new BadRequestException("User is not blocked");
        }

        blockedUserRepository.deleteByBlockerIdAndBlockedId(user, targetUser);
    }

    @Override
    public boolean isBlocked(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User targetUser = userRepository.findById(otherUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return blockedUserRepository.existsByBlockerIdAndBlockedId(user, targetUser) || blockedUserRepository.existsByBlockerIdAndBlockedId(targetUser, user);
    }

    @Override
    public List<FriendResponseDTO> getBlockedUsers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return blockedUserRepository.findByBlockerId(user)
                .stream()
                .map(BlockedUser::getBlockedId)
                .map(userMapper::toFriendResponseDTO)
                .collect(Collectors.toList());
    }
}