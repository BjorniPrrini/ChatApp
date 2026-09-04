package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.AddParticipantToGroupCell;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import lombok.Setter;

public class AddParticipantsController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<ParticipantDTO> friendsList;
    @FXML
    private Label errorLabel;

    @Setter
    private Runnable onBack;
    @Setter
    private Long conversationId;

    private final ObservableList<ParticipantDTO> allFriends = FXCollections.observableArrayList();
    private final FilteredList<ParticipantDTO> filteredFriends = new FilteredList<>(allFriends, _ -> true);

    @FXML
    private void initialize() {
        friendsList.setItems(filteredFriends);

        searchField.textProperty().addListener((_, _, newValue) -> filteredFriends.setPredicate(p -> matches(p, newValue)));

        friendsList.setCellFactory(_ -> new AddParticipantToGroupCell(participantId -> {
            try {
                ConversationService conversationService = new ConversationService();

                conversationService.addParticipant(conversationId, participantId);

                allFriends.removeIf(p -> p.getUserId().equals(participantId));
            } catch (Exception _) {
                AlertUtils.showError(errorLabel, "Couldn't add user");
            }
        }));
    }

    public void loadFriendList(){
        try {
            allFriends.setAll(new ConversationService().getFriendsNotInConversation(conversationId));
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load friends");
        }
    }

    public void handleBack(){
        onBack.run();
    }

    private boolean matches(ParticipantDTO participant, String term){
        String lowerTerm = term.toLowerCase();

        boolean matchesNameSurname = (participant.getName() + " " + participant.getSurname()).toLowerCase().startsWith(lowerTerm);

        boolean matchesNickname = false;

        if(participant.getNickname() != null){
            matchesNickname = participant.getNickname().toLowerCase().startsWith(lowerTerm);
        }

        return matchesNameSurname || matchesNickname;
    }
}