package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.FriendsCell;
import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.AlertUtils;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import lombok.Setter;

import java.util.function.Consumer;

public class MyFriendsController {
    @FXML
    private ListView<FriendResponseDTO> friendsListView;
    @FXML
    private Label errorLabel;

    @Setter
    private Consumer<ConversationResponseDTO> onStartConversation;

    private final FriendService friendService = new FriendService();
    private final ConversationService conversationService = new ConversationService();

    @FXML
    public void initialize(){
        friendsListView.setCellFactory(_ -> new FriendsCell(
                friendId -> {
                    try {
                        ConversationResponseDTO conversation = conversationService.createConversation(friendId);

                        onStartConversation.accept(conversation);
                    } catch (Exception _) {
                        AlertUtils.showError(errorLabel, "Couldn't start conversation");
                    }
                },
                friendId -> {
                    try {
                        friendService.removeFriend(SessionManager.getInstance().getUserId(), friendId);

                        loadFriends();
                    } catch (Exception _) {
                        AlertUtils.showError(errorLabel, "Couldn't remove friend");
                    }
                },
                friendId -> {
                    try {
                        friendService.blockFriend(friendId);

                        loadFriends();
                    } catch (Exception _) {
                        AlertUtils.showError(errorLabel, "Couldn't block friend");
                    }
                }
        ));

        loadFriends();
    }

    private void loadFriends(){
        try {
            friendsListView.getItems().clear();
            friendsListView.getItems().addAll(friendService.getFriends());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load friends");
        }
    }
}