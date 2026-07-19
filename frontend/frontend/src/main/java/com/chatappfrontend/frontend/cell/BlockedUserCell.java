package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class BlockedUserCell extends ListCell<FriendResponseDTO> {
    private final Consumer<Long> onUnblock;

    public BlockedUserCell(Consumer<Long> onUnblock) {
        this.onUnblock = onUnblock;
    }

    @Override
    protected void updateItem(FriendResponseDTO user, boolean empty){
        super.updateItem(user, empty);

        if(empty || user == null){
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName = user.getName().substring(0, 1).toUpperCase() + user.getName().substring(1).toLowerCase() + " " + user.getSurname().substring(0, 1).toUpperCase() + user.getSurname().substring(1).toLowerCase();

        Label avatar = new Label(displayName.substring(0, 1).toUpperCase());

        avatar.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold; " + "-fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button unblockButton = new Button("Unblock");

        unblockButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #00ff88; " + "-fx-border-color: #00ff88; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");

        unblockButton.setOnAction(_ -> onUnblock.accept(user.getSenderId()));

        HBox cell = new HBox(10, avatar, nameLabel, spacer, unblockButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}