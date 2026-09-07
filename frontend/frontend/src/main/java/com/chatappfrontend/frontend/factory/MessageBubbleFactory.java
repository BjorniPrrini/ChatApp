package com.chatappfrontend.frontend.factory;

import com.chatappfrontend.frontend.model.MessageResponseDTO;
import com.chatappfrontend.frontend.util.AvatarUtils;

import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MessageBubbleFactory {
    private final Long currentUserId;
    private final Consumer<MessageResponseDTO> onReply;
    private final Consumer<MessageResponseDTO> onEdit;
    private final BiConsumer<MessageResponseDTO, HBox> onDeleteForMe;
    private final BiConsumer<MessageResponseDTO, HBox> onDeleteForEveryone;

    public MessageBubbleFactory(Long currentUserId, Consumer<MessageResponseDTO> onReply, Consumer<MessageResponseDTO> onEdit, BiConsumer<MessageResponseDTO, HBox> onDeleteForMe, BiConsumer<MessageResponseDTO, HBox> onDeleteForEveryone) {
        this.currentUserId = currentUserId;
        this.onReply = onReply;
        this.onEdit = onEdit;
        this.onDeleteForMe = onDeleteForMe;
        this.onDeleteForEveryone = onDeleteForEveryone;
    }

    public HBox createMessageBubble(MessageResponseDTO message, boolean isGroup){
        HBox hBox = new HBox();

        hBox.getProperties().put("messageId", message.getId());
        hBox.getProperties().put("messageObj", message);

        VBox bubble = new VBox();

        bubble.setSpacing(5);
        bubble.setMaxWidth(400);

        boolean isMyMessage = message.getSenderId().equals(currentUserId);

        if(message.getReplyToId() != null){
            Label replyLabel = new Label(message.getReplyToMessage());

            replyLabel.setWrapText(true);
            replyLabel.setMaxWidth(300);

            replyLabel.setStyle("-fx-background-color: #555555; -fx-text-fill: #dddddd; -fx-padding: 6 8; -fx-background-radius: 8; -fx-font-size: 12px;");

            bubble.getChildren().add(replyLabel);
        }

        Label messageLabel = new Label(message.getMessage());

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        if(isMyMessage){
            messageLabel.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-padding: 8 12; -fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_RIGHT);
        }else{
            messageLabel.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_LEFT);
        }

        bubble.getChildren().add(messageLabel);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem reply = new MenuItem("Reply");

        reply.setOnAction(_ -> onReply.accept(message));

        contextMenu.getItems().add(reply);

        if(isMyMessage){
            MenuItem edit = new MenuItem("Edit");

            edit.setOnAction(_ -> onEdit.accept(message));

            MenuItem deleteForMe = new MenuItem("Delete for me");

            deleteForMe.setOnAction(_ -> onDeleteForMe.accept(message, hBox));

            MenuItem deleteForEveryone = new MenuItem("Delete for everyone");

            deleteForEveryone.setOnAction(_ -> onDeleteForEveryone.accept(message, hBox));

            contextMenu.getItems().addAll(edit, deleteForMe, deleteForEveryone);
        }else{
            MenuItem deleteForMe = new MenuItem("Delete for me");

            deleteForMe.setOnAction(_ -> onDeleteForMe.accept(message, hBox));

            contextMenu.getItems().add(deleteForMe);
        }

        if(!isMyMessage && isGroup){
            Label avatar = new Label();

            String initials = message.getSenderName().substring(0, 1).toUpperCase() + message.getSenderSurname().substring(0, 1).toUpperCase();

            avatar.setStyle("-fx-background-color: #000000FF; -fx-text-fill: #00ff88; -fx-font-weight: bold; -fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30; -fx-background-radius: 50; -fx-alignment: center;");

            AvatarUtils.applyAvatar(avatar, message.getSenderProfilePicture(), initials, true, 30);

            hBox.setSpacing(8);
            hBox.getChildren().add(avatar);
        }

        HBox metadataRow = new HBox(5);

        metadataRow.setAlignment(Pos.CENTER_RIGHT);

        if(message.isEdited()){
            Label editedLabel = new Label("Edited");

            editedLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

            metadataRow.getChildren().add(editedLabel);
        }

        if(isMyMessage){
            Label statusLabel = new Label(formatStatus(message.getStatus()));

            statusLabel.setStyle("-fx-text-fill: " + getStatusColor(message.getStatus()) + "; -fx-font-size: 10px;");

            metadataRow.getChildren().add(statusLabel);

            hBox.getProperties().put("statusLabel", statusLabel);
        }

        if(!metadataRow.getChildren().isEmpty()){
            bubble.getChildren().add(metadataRow);
        }

        messageLabel.setContextMenu(contextMenu);

        hBox.getChildren().add(bubble);

        return hBox;
    }

    public String formatStatus(String status){
        if(status == null){
            return "";
        }

        return switch (status) {
            case "sent" -> "✓";
            case "delivered", "read" -> "✓✓";
            default -> "";
        };
    }

    public String getStatusColor(String status){
        if(status == null){
            return "#888888";
        }

        if(status.equals("read")){
            return "#009aff";
        }

        return "#888888";
    }
}