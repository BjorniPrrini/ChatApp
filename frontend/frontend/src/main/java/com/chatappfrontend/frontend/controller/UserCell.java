package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.UserResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Set;

public class UserCell extends ListCell<UserResponseDTO>{
    private final Set<Long> friendIds;
    private final Set<Long> pendingIds;

    public UserCell(Set<Long> friendIds, Set<Long> pendingIds){
        this.friendIds = friendIds;
        this.pendingIds = pendingIds;
    }

    @Override
    protected void updateItem(UserResponseDTO user, boolean empty){
        super.updateItem(user, empty);

        if(empty || user == null){
            setGraphic(null);

            return;
        }

        String displayName = user.getName().substring(0, 1).toUpperCase() + user.getName().substring(1).toLowerCase() + " " + user.getSurname().substring(0, 1).toUpperCase() + user.getSurname().substring(1).toLowerCase();

        Label avatar = new Label(user.getName().substring(0, 1).toUpperCase() + user.getSurname().substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-font-weight: bold; " + "-fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");


        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);

        Label nicknameLabel = new Label("@" + (user.getNickname() != null ? user.getNickname() : ""));

        nicknameLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, nicknameLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add");

        if(friendIds.contains(user.getId())){
            addButton.setText("Friends");
            addButton.setDisable(true);
            addButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #888888; -fx-background-radius: 5;");
        }else if(pendingIds.contains(user.getId())){
            addButton.setText("Pending");
            addButton.setDisable(true);
            addButton.setStyle("-fx-background-color: #ff9900; -fx-text-fill: black; -fx-background-radius: 5;");
        }else{
            addButton.setText("Add");
            addButton.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand;");
        }

        addButton.setOnAction(_ -> {
            addButton.setDisable(true);
            addButton.setText("Sending...");

            try {
                FriendService friendService = new FriendService();
                friendService.sendFriendRequest(user.getId());

                addButton.setText("Sent");
                addButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #888888; -fx-background-radius: 5;");
            } catch(Exception e) {
                addButton.setDisable(false);
                addButton.setText("Error");
            }
        });

        HBox cell = new HBox(10, avatar, textBox, spacer, addButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}