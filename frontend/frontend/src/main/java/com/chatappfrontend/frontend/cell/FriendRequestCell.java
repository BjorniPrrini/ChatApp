package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.service.FriendService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FriendRequestCell extends ListCell<FriendResponseDTO> {
    private final Runnable onRefresh;

    public FriendRequestCell(Runnable onRefresh) {
        this.onRefresh = onRefresh;
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

        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);

        Label subtitleLabel = new Label("wants to connect");
        subtitleLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptButton = new Button("✓");
        acceptButton.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; " + "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold; -fx-min-width: 35;");

        Button rejectButton = new Button("✗");
        rejectButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ff4444; " + "-fx-border-color: #ff4444; -fx-border-radius: 5; -fx-background-radius: 5; " + "-fx-cursor: hand; -fx-min-width: 35;");

        acceptButton.setOnAction(_ -> {
            try {
                FriendService friendService = new FriendService();
                friendService.acceptFriendRequest(friend.getSenderId());

                onRefresh.run();
            } catch (Exception e) {
                acceptButton.setText("Error");
            }
        });

        rejectButton.setOnAction(_ -> {
            try {
                FriendService friendService = new FriendService();

                friendService.rejectFriendRequest(friend.getSenderId());

                setGraphic(null);
                setManaged(false);
                setVisible(false);
            } catch (Exception e) {
                rejectButton.setText("Error");
            }
        });

        HBox cell = new HBox(10, avatar, textBox, spacer, acceptButton, rejectButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);
        setStyle("-fx-background-color: transparent;");
    }
}