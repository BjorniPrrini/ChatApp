package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.ParticipantDTO;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class AddParticipantToGroupCell extends ListCell<ParticipantDTO> {
    private final Consumer<Long> onAdd;

    public AddParticipantToGroupCell(Consumer<Long> onAdd) {
        this.onAdd = onAdd;
    }

    @Override
    protected void updateItem(ParticipantDTO participant, boolean empty){
        super.updateItem(participant, empty);

        if(empty || participant == null){
            setGraphic(null);

            setStyle("-fx-background-color: transparent;");

            return;
        }

        String displayName = participant.getName() + " " + participant.getSurname();

        Label avatar = new Label(participant.getName().substring(0, 1).toUpperCase());

        avatar.setStyle("-fx-background-color: #00ff88; -fx-text-fill: black; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-alignment: center;");

        Label nameLabel = new Label(displayName);

        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        nameLabel.setWrapText(false);
        nameLabel.setMaxWidth(150);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add");

        addButton.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ff4444; -fx-border-color: #ff4444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-min-width: 35;");

        addButton.setOnAction(_ -> onAdd.accept(participant.getUserId()));

        HBox cell = new HBox(10, avatar, nameLabel, spacer, addButton);

        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 8 5;");

        setGraphic(cell);

        setStyle("-fx-background-color: transparent;");
    }
}