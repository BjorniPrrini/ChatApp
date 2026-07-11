package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.UserResponseDTO;
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

public class UserCell extends ListCell<UserResponseDTO> {
    @Override
    protected void updateItem(UserResponseDTO user, boolean empty) {
        super.updateItem(user, empty);

        if(empty || user == null){
            setGraphic(null);

            return;
        }

        Circle avatar = new Circle(20);

        Image image = new Image("http://localhost:8080/avatars/" + user.getProfilePicture(), true);

        avatar.setFill(new ImagePattern(image));

        VBox textBox = getVBox(user);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add");

        addButton.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand;");
        addButton.setOnAction(_ -> {
            try {
                FriendService friendService = new FriendService();

                friendService.sendFriendRequest(user.getId());

                addButton.setText("Sent");
                addButton.setDisable(true);
                addButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #888888; -fx-background-radius: 5;");
            } catch (Exception e) {
                addButton.setText("Error");
            }
        });

        HBox cell = new HBox(10, avatar, textBox, spacer, addButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }

    private static VBox getVBox(UserResponseDTO user) {
        String displayName = user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getName() + " " + user.getSurname();

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label nicknameLabel = new Label("@" + (user.getNickname() != null ? user.getNickname() : ""));

        nicknameLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, nicknameLabel);
        return textBox;
    }
}