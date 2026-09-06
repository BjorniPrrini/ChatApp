package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.UserCell;
import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.model.UserResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.service.UserService;
import com.chatappfrontend.frontend.util.AlertUtils;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AddFriendsController {
    @FXML
    private TextField searchField;
    @FXML
    private Label errorLabel;
    @FXML
    private ListView<UserResponseDTO> searchResultsListView;

    private final UserService userService = new UserService();
    private final FriendService friendService = new FriendService();

    @FXML
    public void initialize(){
        searchResultsListView.setCellFactory(_ -> new UserCell(fetchFriendIds(), fetchPendingIds()));

        searchField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                String term = searchField.getText().trim();

                if(term.length() >= 2){
                    searchUsers(term);
                }
            }
        });
    }

    private Set<Long> fetchFriendIds(){
        try {
            return friendService.getFriends()
                    .stream()
                    .map(fr -> fr.getSenderId().equals(SessionManager.getInstance().getUserId()) ? fr.getReceiverId() : fr.getSenderId())
                    .collect(Collectors.toSet());
        } catch (Exception _) {
            return Set.of();
        }
    }

    private Set<Long> fetchPendingIds(){
        try {
            List<FriendResponseDTO> sent = friendService.getSentRequests();

            return sent.stream()
                    .map(FriendResponseDTO::getReceiverId)
                    .collect(Collectors.toSet());
        } catch (Exception _) {
            return Set.of();
        }
    }

    private void searchUsers(String term){
        try {
            searchResultsListView.getItems().clear();
            searchResultsListView.getItems().addAll(
                    userService.searchUsers(term)
                    .stream()
                    .filter(u -> !u.getId().equals(SessionManager.getInstance().getUserId()))
                    .toList()
            );
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't search for user");
        }
    }
}