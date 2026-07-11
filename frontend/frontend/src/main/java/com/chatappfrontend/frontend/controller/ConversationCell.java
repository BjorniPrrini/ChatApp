package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.image.ImageView;

public class ConversationCell extends ListCell<ConversationResponseDTO> {
    @Override
    protected void updateItem(ConversationResponseDTO conversation, boolean empty) {
        super.updateItem(conversation, empty);

        if(empty || conversation == null){
            setGraphic(null);

            return;
        }

        ImageView avatar = new ImageView(conversation.getProfilePicture());

        avatar.setFitWidth(45);
        avatar.setFitHeight(45);

        Label nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label lastMessageLabel = new Label();
        lastMessageLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        String displayName = conversation.getNickname() != null && !conversation.getNickname().isEmpty() ? conversation.getNickname() : conversation.getName() + " " + conversation.getSurname();

        nameLabel.setText(displayName);
        lastMessageLabel.setText(conversation.getLastMessage() != null ? conversation.getLastMessage() : "No messages yet");

        VBox textBox = new VBox(3, nameLabel, lastMessageLabel);

        HBox cell = new HBox(10, avatar, textBox);

        cell.setAlignment(Pos.CENTER_LEFT);

        setGraphic(cell);
    }
}