package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.util.AlertUtils;

import com.chatappfrontend.frontend.util.TriConsumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import lombok.Setter;

import java.util.function.BiConsumer;

public class GroupInfoController {
    @FXML
    private Button saveButton;
    @FXML
    private Label groupPictureView;
    @FXML
    private Button changePictureButton;
    @FXML
    private TextField groupNameField;
    @FXML
    private Button addParticipantButton;
    @FXML
    private ListView<ParticipantDTO> participantList;
    @FXML
    private Label errorLabel;

    @Setter
    private Long currentConversationId;
    @Setter
    private Runnable onBack;
    @Setter
    private TriConsumer<Long, String, String> onGroupUpdated;

    public void loadGroupInformation(){
        try {
            ConversationService conversationService = new ConversationService();

            ConversationResponseDTO conversationInfo = conversationService.getConversationById(currentConversationId);

            groupPictureView.setText(conversationInfo.getGroupName().substring(0, 1));

            groupPictureView.setStyle("-fx-background-radius: 50%; -fx-text-fill: white; -fx-background-color: black");

            groupNameField.setText(conversationInfo.getGroupName());

            participantList.getItems().setAll(conversationInfo.getParticipants());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load group information");
        }
    }

    @FXML
    public void handleBack(){
        onBack.run();
    }

    @FXML
    public void handleChangePicture(){
        groupPictureView.setText("Changed");
    }

    @FXML
    public void handleAddParticipant(){

    }

    @FXML
    public void handleSave(){
        String groupName = groupNameField.getText().trim();
        String profilePictureName = groupPictureView.getText().trim();

        try {
            ConversationService conversationService = new ConversationService();

            conversationService.updateGroupInfo(currentConversationId, groupName, profilePictureName);

            onGroupUpdated.accept(currentConversationId, groupName, profilePictureName);
        } catch (Exception _) {

        }
    }
}