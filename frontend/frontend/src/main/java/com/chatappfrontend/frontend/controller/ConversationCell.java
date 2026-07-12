package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConversationCell extends ListCell<ConversationResponseDTO> {
    @Override
    protected void updateItem(ConversationResponseDTO conversation, boolean empty) {
        super.updateItem(conversation, empty);

        if(empty || conversation == null){
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName = conversation.getName().substring(0, 1).toUpperCase() + conversation.getName().substring(1).toLowerCase() + " " + conversation.getSurname().substring(0, 1).toUpperCase() + conversation.getSurname().substring(1).toLowerCase();

        Label avatar = new Label(displayName.substring(0, 1).toUpperCase());

        avatar.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-font-weight: bold; " + "-fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        String lastMsg = conversation.getLastMessage() != null ? conversation.getLastMessage() : "No messages yet";

        Label lastMessageLabel = new Label(lastMsg);

        lastMessageLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, lastMessageLabel);

        HBox cell = new HBox(10, avatar, textBox);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}