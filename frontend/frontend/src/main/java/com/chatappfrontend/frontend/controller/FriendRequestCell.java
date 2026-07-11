package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class FriendRequestCell extends ListCell<FriendResponseDTO> {

    @Override
    protected void updateItem(FriendResponseDTO friend, boolean empty) {
        super.updateItem(friend, empty);

        if(empty || friend == null){
            setGraphic(null);

            return;
        }

        Circle avatar = new Circle(20);

        Image image = new Image("http://localhost:8080/avatars/" + friend.getProfilePicture(), true);

        avatar.setFill(new ImagePattern(image));

        String displayName = friend.getNickname() != null && !friend.getNickname().isEmpty() ? friend.getNickname() : friend.getName() + " " + friend.getSurname();

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label subtitleLabel = new Label("wants to connect");

        subtitleLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, subtitleLabel);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptButton = new Button("✓");

        acceptButton.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");

        Button rejectButton = new Button("✗");

        rejectButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ff4444; -fx-border-color: #ff4444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");

        acceptButton.setOnAction(_ -> {
            try {
                FriendService friendService = new FriendService();

                friendService.acceptFriendRequest(friend.getSenderId());

                acceptButton.setText("Added");
                acceptButton.setDisable(true);
                rejectButton.setDisable(true);
                acceptButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #888888; -fx-background-radius: 5;");
            } catch (Exception e) {
                acceptButton.setText("Error");
            }
        });

        rejectButton.setOnAction(_ -> {
            try {
                FriendService friendService = new FriendService();

                friendService.rejectFriendRequest(friend.getSenderId());

                setGraphic(null);
                setManaged(false);
                setVisible(false);
            } catch (Exception e) {
                rejectButton.setText("Error");
            }
        });

        HBox cell = new HBox(10, avatar, textBox, spacer, acceptButton, rejectButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}