package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.SuggestedFriendCell;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class SuggestedFriendsController {
    @FXML
    private Label errorLabel;
    @FXML
    private ListView<FriendResponseDTO> suggestedFriendsListView;

    private final FriendService friendService = new FriendService();

    @FXML
    public void initialize(){
        suggestedFriendsListView.setCellFactory(_ -> new SuggestedFriendCell());

        loadSuggested();
    }

    private void loadSuggested(){
        try {
            suggestedFriendsListView.getItems().clear();
            suggestedFriendsListView.getItems().addAll(friendService.getSuggestions());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load suggested friends");
        }
    }
}