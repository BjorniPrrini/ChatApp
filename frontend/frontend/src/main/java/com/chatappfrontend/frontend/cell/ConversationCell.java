package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.util.AvatarUtils;

import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ConversationCell extends ListCell<ConversationResponseDTO> {
    private final Consumer<Long> onDelete;

    public ConversationCell(Consumer<Long> onDelete) {
        this.onDelete = onDelete;
    }

    @Override
    protected void updateItem(ConversationResponseDTO conversation, boolean empty) {
        super.updateItem(conversation, empty);

        if(empty || conversation == null){
            setGraphic(null);

            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName;
        String borderColor;
        String profilePicturePath;

        if(conversation.isGroup()){
            displayName = conversation.getGroupName();

            profilePicturePath = conversation.getGroupPicture();

            borderColor = "-app-border-strong";
        }else{
            ParticipantDTO otherUser = conversation.getParticipants().getFirst();

            displayName = otherUser.getName().substring(0, 1).toUpperCase() + otherUser.getName().substring(1).toLowerCase() + " " + otherUser.getSurname().substring(0, 1).toUpperCase() + otherUser.getSurname().substring(1).toLowerCase();

            profilePicturePath = otherUser.getProfilePicture();

            borderColor = otherUser.isOnline() ? "-app-accent" : "-app-border-strong";
        }

        HBox cell = buildCell(conversation, displayName, profilePicturePath, borderColor);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }

    private HBox buildCell(ConversationResponseDTO conversation, String displayName, String profilePicturePath, String borderColor){
        Label avatar = new Label();

        String initial = displayName.substring(0, 1);

        avatar.setStyle("-fx-background-color: -app-bg; -fx-text-fill: -app-accent; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 50; -fx-alignment: center; -fx-border-color: " + borderColor + "; -fx-border-radius: 20; -fx-border-width: 2;");

        AvatarUtils.applyAvatar(avatar, profilePicturePath, initial, true, 40);

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: -app-text; -fx-font-weight: bold; -fx-font-size: 13px;");

        String lastMsg = conversation.getLastMessage() != null ? conversation.getLastMessage() : "No messages yet";

        Label lastMessageLabel = new Label(lastMsg);

        lastMessageLabel.setStyle("-fx-text-fill: -app-text-muted; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, lastMessageLabel);

        HBox cell = new HBox(10, avatar, textBox);

        MenuItem deleteItem = new MenuItem("Delete conversation");

        deleteItem.setOnAction(_ -> onDelete.accept(conversation.getConversationId()));

        ContextMenu menu = new ContextMenu();

        menu.getItems().add(deleteItem);

        cell.setOnContextMenuRequested(event -> menu.show(cell, event.getScreenX(), event.getScreenY()));

        return cell;
    }
}