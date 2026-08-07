package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.FriendRequest;
import com.chatappbackend.backend.entity.User;

import org.springframework.stereotype.Component;

@Component
public class FriendRequestMapper {
    private final UserMapper userMapper;

    public FriendRequestMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public FriendResponseDTO toFriendResponseDTO(FriendRequest friendRequest, Long currentUserId){
        User otherUser = friendRequest.getSender().getId().equals(currentUserId) ? friendRequest.getReceiver() : friendRequest.getSender();

        FriendResponseDTO response = userMapper.toFriendResponseDTO(otherUser);

        response.setRequestId(friendRequest.getId());
        response.setReceiverId(friendRequest.getReceiver().getId());
        response.setSenderId(friendRequest.getSender().getId());
        response.setCreatedAt(friendRequest.getCreatedAt());
        response.setStatus(friendRequest.getStatus());

        return response;
    }
}