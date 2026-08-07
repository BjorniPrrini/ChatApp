package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.dto.user.UserResponseDTO;
import com.chatappbackend.backend.entity.User;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDTO toUserResponseDTO(User user){
        UserResponseDTO response = new UserResponseDTO();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setProfilePicture(user.getProfilePicture());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setOnline(user.isOnline());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    public FriendResponseDTO toFriendResponseDTO(User user){
        FriendResponseDTO response = new FriendResponseDTO();

        response.setSenderId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setNickname(user.getNickname());
        response.setProfilePicture(user.getProfilePicture());

        return response;
    }
}