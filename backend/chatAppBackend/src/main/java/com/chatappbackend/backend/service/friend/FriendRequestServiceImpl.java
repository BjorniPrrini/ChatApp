package com.chatappbackend.backend.service.friend;

import com.chatappbackend.backend.dto.friend.FriendResponseDTO;
import com.chatappbackend.backend.entity.FriendRequest;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
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

    public FriendRequestServiceImpl(UserRepository userRepository, BlockedUserService blockedUserService, FriendRequestRepository friendRequestRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.blockedUserService = blockedUserService;
        this.friendRequestRepository = friendRequestRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void sendFriendRequest(Long userId, Long receiverId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(blockedUserService.isBlocked(userId, receiverId)){
            throw new BadRequestException("User is blocked");
        }

        if(friendRequestRepository.existsBySenderIdAndReceiverId(userId, receiverId)){
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
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getFriends(Long userId) {
        return friendRequestRepository.findAcceptedFriendships(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getSuggestedFriends(Long userId) {
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();

        visited.add(userId);

        Set<Long> suggested = new HashSet<>();

        List<FriendRequest> myFriendships = friendRequestRepository.findAcceptedFriendships(userId);

        for(FriendRequest friendship : myFriendships){
            Long friendId = friendship.getSender().getId().equals(userId) ? friendship.getReceiver().getId() : friendship.getSender().getId();

            queue.add(friendId);
            visited.add(friendId);
        }

        while(!queue.isEmpty()){
            Long currentFriendId = queue.poll();

            List<FriendRequest> theirFriendships = friendRequestRepository.findAcceptedFriendships(currentFriendId);

            for(FriendRequest friendsFriend : theirFriendships){
                Long theirFriendId = friendsFriend.getSender().getId().equals(currentFriendId) ? friendsFriend.getReceiver().getId() : friendsFriend.getSender().getId();

                if(!visited.contains(theirFriendId)){
                    suggested.add(theirFriendId);
                    visited.add(theirFriendId);
                }
            }
        }


        return suggested.stream()
                .map(suggestedId -> userRepository.findById(suggestedId).orElseThrow())
                .map(suggestedUser -> {
                    FriendResponseDTO dto = new FriendResponseDTO();

                    dto.setSenderId(suggestedUser.getId());
                    dto.setName(suggestedUser.getName());
                    dto.setSurname(suggestedUser.getSurname());
                    dto.setNickname(suggestedUser.getNickname());
                    dto.setProfilePicture(suggestedUser.getProfilePicture());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getSentRequests(Long userId) {
        return friendRequestRepository.findBySenderIdAndStatus(userId, "pending")
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FriendResponseDTO mapToDTO(FriendRequest friendRequest){
        FriendResponseDTO response = new FriendResponseDTO();

        response.setRequestId(friendRequest.getId());
        response.setReceiverId(friendRequest.getReceiver().getId());
        response.setSenderId(friendRequest.getSender().getId());
        response.setName(friendRequest.getSender().getName());
        response.setSurname(friendRequest.getSender().getSurname());
        response.setNickname(friendRequest.getSender().getNickname());
        response.setProfilePicture(friendRequest.getSender().getProfilePicture());
        response.setCreatedAt(friendRequest.getCreatedAt());
        response.setStatus(friendRequest.getStatus());

        return response;
    }
}