package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class FriendsCell extends ListCell<FriendResponseDTO> {
    private final Consumer<Long> onStartChat;
    private final Consumer<Long> onRemoveFriend;
    private final Consumer<Long> onBlockFriend;

    public FriendsCell(Consumer<Long> onStartChat, Consumer<Long> onRemoveFriend, Consumer<Long> onBlockFriend) {
        this.onStartChat = onStartChat;
        this.onRemoveFriend = onRemoveFriend;
        this.onBlockFriend = onBlockFriend;
    }

    @Override
    protected void updateItem(FriendResponseDTO friend, boolean empty){
        super.updateItem(friend, empty);

        if(empty || friend == null){
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName = friend.getName().substring(0, 1).toUpperCase() + friend.getName().substring(1).toLowerCase() + " " + friend.getSurname().substring(0, 1).toUpperCase() + friend.getSurname().substring(1).toLowerCase();

        Label avatar = new Label(displayName.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-font-weight: bold; " + "-fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        nameLabel.setWrapText(false);
        nameLabel.setMaxWidth(150);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        Button openMenu = new Button("...");

        openMenu.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ff4444; " + "-fx-border-color: #ff4444; -fx-border-radius: 5; -fx-background-radius: 5; " + "-fx-cursor: hand; -fx-min-width: 35;");

        openMenu.setOnAction(_ -> {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem startChat = new MenuItem("Start Chat");

            startChat.setOnAction(_ -> {
                Long currentUserId = SessionManager.getInstance().getUserId();

                Long otherUserId;

                if(friend.getSenderId().equals(currentUserId)){
                    otherUserId = friend.getReceiverId();
                }else{
                    otherUserId = friend.getSenderId();
                }

                onStartChat.accept(otherUserId);
            });

            MenuItem removeFriend = new MenuItem("Remove Friend");

            removeFriend.setOnAction(_ -> {
                Long currentUserID = SessionManager.getInstance().getUserId();

                Long otherUserId = friend.getSenderId().equals(currentUserID) ? friend.getReceiverId() : friend.getSenderId();

                onRemoveFriend.accept(otherUserId);
            });

            MenuItem block = new MenuItem("Block");

            block.setOnAction(_ -> {
                Long currentUserId = SessionManager.getInstance().getUserId();

                Long otherUserId = friend.getSenderId().equals(currentUserId) ? friend.getReceiverId() : friend.getSenderId();

                onBlockFriend.accept(otherUserId);
            });

            contextMenu.getItems().addAll(startChat, removeFriend, block);
            contextMenu.show(openMenu, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        HBox cell = new HBox(10, avatar, nameLabel, openMenu);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}