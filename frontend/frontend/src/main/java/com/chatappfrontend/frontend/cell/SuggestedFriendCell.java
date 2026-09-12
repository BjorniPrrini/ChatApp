package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.util.AvatarUtils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;

public class SuggestedFriendCell extends ListCell<FriendResponseDTO> {
    private final FriendService friendService = new FriendService();

    @Override
    public void updateItem(FriendResponseDTO friend, boolean empty){
        super.updateItem(friend, empty);

        if(empty || friend == null){
            setGraphic(null);

            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName = friend.getName().substring(0, 1).toUpperCase() + friend.getName().substring(1).toLowerCase() + " " + friend.getSurname().substring(0, 1).toUpperCase() + friend.getSurname().substring(1).toLowerCase();

        Label avatar = new Label(displayName.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: -app-accent; -fx-text-fill: black; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        String initials = friend.getName().substring(0, 1) + friend.getSurname().substring(0, 1);

        AvatarUtils.applyAvatar(avatar, friend.getProfilePicture(), initials, false, 40);

        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-text-fill: -app-text; -fx-font-weight: bold; -fx-font-size: 13px;");

        nameLabel.setWrapText(false);
        nameLabel.setMaxWidth(150);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        Button addFriend = new Button("Add");

        addFriend.setStyle("-fx-background-color: -app-bg-input; -fx-text-fill: -app-danger; -fx-border-color: -app-danger; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-min-width: 35;");

        addFriend.setOnAction(_ -> {
            addFriend.setDisable(true);
            addFriend.setText("Sending...");

            try {
                friendService.sendFriendRequest(friend.getSenderId());

                addFriend.setText("Sent");
                addFriend.setStyle("-fx-background-color: -app-bg-disabled; -fx-text-fill: -app-text-muted; -fx-background-radius: 5;");
            } catch (Exception _) {
                addFriend.setDisable(false);
                addFriend.setText("Error");
            }
        });

        HBox cell = new HBox(10, avatar, nameLabel, addFriend);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}