package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.FriendRequestCell;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class FriendRequestsController {
    @FXML
    private Label errorLabel;
    @FXML
    private ListView<FriendResponseDTO> friendRequestsListView;

    @FXML
    public void initialize(){
        friendRequestsListView.setCellFactory(_ -> new FriendRequestCell(this::loadFriendRequests));

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        try {
            FriendService friendService = new FriendService();

            friendRequestsListView.getItems().clear();
            friendRequestsListView.getItems().addAll(friendService.getFriendRequests());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load friend requests");
        }
    }
}