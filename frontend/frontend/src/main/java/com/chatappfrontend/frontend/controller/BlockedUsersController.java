package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.BlockedUserCell;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class BlockedUsersController {
    @FXML
    private Label errorLabel;
    @FXML
    private ListView<FriendResponseDTO> blockedUsersListView;

    private final FriendService friendService = new FriendService();

    @FXML
    public void initialize(){
        blockedUsersListView.setCellFactory(_ -> new BlockedUserCell(userId -> {
            try {
                friendService.unblockUser(userId);

                loadBlockedUsers();
            } catch (Exception _) {
                AlertUtils.showError(errorLabel, "Couldn't unblock user");
            }
        }));

        loadBlockedUsers();
    }

    private void loadBlockedUsers(){
        try {
            blockedUsersListView.getItems().clear();
            blockedUsersListView.getItems().addAll(friendService.getBlockedUsers());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load blocked users");
        }
    }
}