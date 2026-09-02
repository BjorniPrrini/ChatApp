package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.ParticipantDTO;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ParticipantCell extends ListCell<ParticipantDTO> {
    private final boolean viewerIsAdmin;
    private final Consumer<Long> onPromote;
    private final Consumer<Long> onDemote;
    private final Consumer<Long> onKick;

    public ParticipantCell(boolean viewerIsAdmin, Consumer<Long> onPromote, Consumer<Long> onDemote, Consumer<Long> onKick) {
        this.viewerIsAdmin = viewerIsAdmin;
        this.onPromote = onPromote;
        this.onDemote = onDemote;
        this.onKick = onKick;
    }

    @Override
    protected void updateItem(ParticipantDTO participant, boolean empty){
        super.updateItem(participant, empty);

        if(empty || participant == null){
            setGraphic(null);

            setStyle("-fx-background-color: transparent;");

            return;
        }

        Label avatar = new Label(participant.getName().substring(0, 1).toUpperCase());

        avatar.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        Label nameLabel = new Label(participant.getName() + " " + participant.getSurname());

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label nicknameLabel = new Label(participant.getNickname());

        nicknameLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        VBox textBox = new VBox(3, nameLabel, nicknameLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(10, avatar, textBox, spacer);

        if(viewerIsAdmin){
            Button openMenu = new Button("...");

            openMenu.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ff4444; -fx-border-color: #ff4444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-min-width: 35;");

            openMenu.setOnAction(_ -> {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem promoteItem = new MenuItem("Promote to admin");

                promoteItem.setOnAction(_ -> onPromote.accept(participant.getUserId()));

                MenuItem demoteItem = new MenuItem("Demote from admin");

                demoteItem.setOnAction(_ -> onDemote.accept(participant.getUserId()));

                MenuItem kickItem = new MenuItem("Kick from group");

                kickItem.setOnAction(_ -> onKick.accept(participant.getUserId()));

                contextMenu.getItems().addAll(promoteItem, demoteItem, kickItem);
                contextMenu.show(openMenu, javafx.geometry.Side.BOTTOM, 0, 0);
            });

            cell.getChildren().add(openMenu);
        }

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);

        setStyle("-fx-background-color: transparent;");
    }
}