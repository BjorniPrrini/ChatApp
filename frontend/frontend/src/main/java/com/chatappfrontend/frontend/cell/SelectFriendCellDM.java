package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class SelectFriendCellDM extends ListCell<FriendResponseDTO> {
    @Override
    protected void updateItem(FriendResponseDTO friend, boolean empty){
        super.updateItem(friend, empty);

        if(empty || friend == null){
            setGraphic(null);

            setStyle("-fx-background-color: transparent;");

            return;
        }

        Label nameLabel = new Label(friend.getName() + " " + friend.getSurname());

        nameLabel.setStyle("-fx-text-fill: white");

        HBox box = new HBox(nameLabel);

        box.setAlignment(Pos.CENTER_LEFT);

        box.setStyle("-fx-padding: 10; -fx-background-color: #222222; -fx-background-radius: 8;");

        setGraphic(box);

        setStyle("-fx-background-color: transparent;");
    }
}