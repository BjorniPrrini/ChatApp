package com.chatappbackend.backend.service.friend;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.FriendRequest;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.mapper.FriendRequestMapper;
import com.chatappbackend.backend.mapper.UserMapper;
import com.chatappbackend.backend.repository.FriendRequestRepository;
import com.chatappbackend.backend.repository.UserRepository;
import com.chatappbackend.backend.service.blocked.BlockedUserService;
import com.chatappbackend.backend.service.notification.NotificationService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendRequestServiceImpl implements FriendRequestService{
    private final UserRepository userRepository;
    private final BlockedUserService blockedUserService;
    private final FriendRequestRepository friendRequestRepository;
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final FriendRequestMapper friendRequestMapper;

    public FriendRequestServiceImpl(UserRepository userRepository, BlockedUserService blockedUserService, FriendRequestRepository friendRequestRepository, NotificationService notificationService, UserMapper userMapper, FriendRequestMapper friendRequestMapper) {
        this.userRepository = userRepository;
        this.blockedUserService = blockedUserService;
        this.friendRequestRepository = friendRequestRepository;
        this.notificationService = notificationService;
        this.userMapper = userMapper;
        this.friendRequestMapper = friendRequestMapper;
    }

    @Override
    public void sendFriendRequest(Long userId, Long receiverId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(blockedUserService.isBlocked(userId, receiverId)){
            throw new BadRequestException("User is blocked");
        }

        if(friendRequestRepository.existsBySenderIdAndReceiverId(userId, receiverId) || friendRequestRepository.existsBySenderIdAndReceiverId(receiverId, userId)){
            throw new BadRequestException("Request already sent");
        }

        if(userId.equals(receiverId)){
            throw new BadRequestException("You cannot send a friend request to yourself");
        }

        FriendRequest friendRequest = new FriendRequest();

        friendRequest.setSender(user);
        friendRequest.setReceiver(receiver);
        friendRequest.setStatus("pending");
        friendRequest.setCreatedAt(LocalDateTime.now());

        friendRequestRepository.save(friendRequest);

        notificationService.notifyUser(receiverId, "FRIEND_REQUEST_SEND", "Friend request", "You have a new friend request by " + user.getName() + " " + user.getSurname());
    }

    @Override
    public void acceptFriendRequest(Long userId, Long senderId) {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, userId).orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if(friendRequest.getStatus().equals("accepted")){
            throw new BadRequestException("Already accepted");
        }

        if(friendRequest.getStatus().equals("rejected")){
            throw new BadRequestException("Request has been rejected");
        }

        friendRequest.setStatus("accepted");

        friendRequestRepository.save(friendRequest);

        notificationService.notifyUser(friendRequest.getSender().getId(), "FRIEND_REQUEST_ACCEPTED", "Friend request accepted", friendRequest.getReceiver().getName() + " " + friendRequest.getReceiver().getSurname() + " accepted your friend request");
    }

    @Override
    public void rejectFriendRequest(Long userId, Long senderId) {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, userId).orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if(friendRequest.getStatus().equals("accepted")){
            throw new BadRequestException("Already accepted");
        }

        friendRequestRepository.delete(friendRequest);

        notificationService.notifyUser(friendRequest.getSender().getId(), "FRIEND_REQUEST_REJECTED", "Friend request rejected", friendRequest.getReceiver().getName() + " " + friendRequest.getReceiver().getSurname() + " rejected your friend request");
    }

    @Override
    public List<FriendResponseDTO> getFriendRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, "pending")
                .stream()
                .map(fr -> friendRequestMapper.toFriendResponseDTO(fr, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getFriends(Long userId) {
        return friendRequestRepository.findAcceptedFriendships(userId)
                .stream()
                .map(fr -> friendRequestMapper.toFriendResponseDTO(fr, userId))
                .filter(dto -> {
                    Long otherUserId = dto.getSenderId().equals(userId) ? dto.getReceiverId() : dto.getSenderId();

                    return !blockedUserService.isBlocked(userId, otherUserId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getSuggestedFriends(Long userId) {
        Set<Long> visited = new HashSet<>();

        visited.add(userId);

        List<FriendRequest> myFriendships = friendRequestRepository.findAcceptedFriendships(userId);

        for(FriendRequest friendship : myFriendships){
            Long friendId = friendship.getSender().getId().equals(userId) ? friendship.getReceiver().getId() : friendship.getSender().getId();

            visited.add(friendId);
        }

        List<FriendRequest> suggestionFriends = friendRequestRepository.findAcceptedFriendshipsForUsers(visited);

        Set<Long> suggestedIds = new HashSet<>();

        for(FriendRequest fr : suggestionFriends){
            Long senderId = fr.getSender().getId();
            Long receiverId = fr.getReceiver().getId();

            if(!visited.contains(senderId)){
                suggestedIds.add(senderId);
            }else if(!visited.contains(receiverId)){
                suggestedIds.add(receiverId);
            }
        }

        List<User> suggestedUsers = userRepository.findAllById(suggestedIds);

        return suggestedUsers.stream()
                .map(userMapper::toFriendResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getSentRequests(Long userId) {
        return friendRequestRepository.findBySenderIdAndStatus(userId, "pending")
                .stream()
                .map(fr -> friendRequestMapper.toFriendResponseDTO(fr, userId))
                .collect(Collectors.toList());
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        FriendRequest friendRequest = friendRequestRepository.findAcceptedFriendship(userId, friendId).orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        friendRequestRepository.delete(friendRequest);
    }
}