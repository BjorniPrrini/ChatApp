package com.chatappfrontend.frontend.cell;

import com.chatappfrontend.frontend.model.FriendResponseDTO;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.Set;

public class SelectFriendCellGroup extends ListCell<FriendResponseDTO> {
    private final Set<FriendResponseDTO> selectedFriends;
    private final Runnable onSelectionChanged;

    public SelectFriendCellGroup(Set<FriendResponseDTO> selectedFriends, Runnable onSelectionChanged) {
        this.selectedFriends = selectedFriends;
        this.onSelectionChanged = onSelectionChanged;

        addEventFilter(MouseEvent.MOUSE_PRESSED, MouseEvent::consume);
    }

    @Override
    protected void updateItem(FriendResponseDTO friend, boolean empty){
        super.updateItem(friend, empty);

        if(empty || friend == null){
            setGraphic(null);
            setOnMouseClicked(null);
            setStyle("-fx-background-color: transparent;");

            return;
        }

        Label nameLabel = new Label(friend.getName() + " " + friend.getSurname());

        nameLabel.setStyle("-fx-text-fill: white");

        HBox box = new HBox(nameLabel);

        box.setAlignment(Pos.CENTER_LEFT);

        applyStyle(box, friend);

        setGraphic(box);
        setStyle("-fx-background-color: transparent;");

        setOnMouseClicked(_ -> {
            if(selectedFriends.contains(friend)){
                selectedFriends.remove(friend);
            }else{
                selectedFriends.add(friend);
            }

            applyStyle(box, friend);

            if(onSelectionChanged != null){
                onSelectionChanged.run();
            }
        });
    }

    private void applyStyle(HBox box, FriendResponseDTO friend){
        if(selectedFriends.contains(friend)){
            box.setStyle("-fx-padding: 10; -fx-background-color: #222222; -fx-background-radius: 8; -fx-border-color: #00ff88; -fx-border-width: 2; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, #00ff88, 2, 0.5, 0, 0);");
        }else{
            box.setStyle("-fx-padding: 10; -fx-background-color: #222222; -fx-background-radius: 8;");
        }
    }
}