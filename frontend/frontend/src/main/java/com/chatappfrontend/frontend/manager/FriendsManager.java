package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.cell.FriendRequestCell;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.scene.control.ListView;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FriendsManager {
    private final ListView<FriendResponseDTO> friendsList;
    private final ListView<FriendResponseDTO> friendRequestsList;
    private final ListView<FriendResponseDTO> blockedUsersList;
    private final Consumer<String> onError;

    @Getter
    private final Set<Long> friendIds = new HashSet<>();
    @Getter
    private final Set<Long> pendingIds = new HashSet<>();

    public FriendsManager(ListView<FriendResponseDTO> friendsList, ListView<FriendResponseDTO> friendRequestsList, ListView<FriendResponseDTO> blockedUsersList, Consumer<String> onError) {
        this.friendsList = friendsList;
        this.friendRequestsList = friendRequestsList;
        this.blockedUsersList = blockedUsersList;
        this.onError = onError;
    }

    public void loadFriendRequests() {
        try {
            FriendService friendService = new FriendService();
            List<FriendResponseDTO> friendRequests = friendService.getFriendRequests();

            friendRequestsList.setCellFactory(_ -> new FriendRequestCell(() -> {
                loadFriendRequests();
                loadFriends();
            }));

            friendRequestsList.getItems().clear();
            friendRequestsList.getItems().addAll(friendRequests);
        } catch (Exception e) {
            onError.accept("Couldn't get friend requests");
        }
    }

    public void loadFriends(){
        try {
            FriendService friendService = new FriendService();

            List<FriendResponseDTO> friends = friendService.getFriends();

            friendsList.getItems().clear();
            friendsList.getItems().addAll(friends);
        } catch (Exception e) {
            onError.accept("Couldn't get friends");
        }
    }

    public void loadBlockedUsers(){
        try {
            FriendService friendService = new FriendService();

            List<FriendResponseDTO> blocked = friendService.getBlockedUsers();

            blockedUsersList.getItems().clear();
            blockedUsersList.getItems().addAll(blocked);
        } catch (Exception e) {
            onError.accept("Couldn't get blocked users");
        }
    }

    public void showFriends(){
        try {
            FriendService friendService = new FriendService();

            friendIds.clear();
            pendingIds.clear();

            Long currentUserId = SessionManager.getInstance().getUserId();

            friendService.getFriends().forEach(f -> {
                Long friendId = f.getSenderId().equals(currentUserId) ? f.getReceiverId() : f.getSenderId();

                friendIds.add(friendId);
            });

            friendService.getSentRequests().forEach(f -> pendingIds.add(f.getReceiverId()));
        } catch (Exception e) {
            onError.accept("Could not load friend status");
        }

        loadFriendRequests();
        loadFriends();
        loadBlockedUsers();
    }
}