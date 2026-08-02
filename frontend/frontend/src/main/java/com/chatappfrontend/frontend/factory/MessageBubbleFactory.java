package com.chatappfrontend.frontend.factory;

import com.chatappfrontend.frontend.model.MessageResponseDTO;

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

    public HBox createMessageBubble(MessageResponseDTO message){
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

        if(isMyMessage){
            Label statusLabel = new Label(formatStatus(message.getStatus()));

            statusLabel.setStyle("-fx-text-fill: " + getStatusColor(message.getStatus()) + "; -fx-font-size: 10px;");

            statusLabel.setAlignment(Pos.CENTER_RIGHT);

            hBox.getProperties().put("statusLabel", statusLabel);

            bubble.getChildren().add(statusLabel);
        }

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