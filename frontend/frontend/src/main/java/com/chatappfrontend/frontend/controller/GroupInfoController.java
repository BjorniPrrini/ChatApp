package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.ParticipantCell;
import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.util.AlertUtils;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.TriConsumer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import lombok.Setter;

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
    @FXML
    private CheckBox allowInviteToggle;

    @Setter
    private Long currentConversationId;
    @Setter
    private Runnable onBack;
    @Setter
    private TriConsumer<Long, String, String> onGroupUpdated;
    @Setter
    private StackPane contentPane;

    private boolean isAdmin;

    public void loadGroupInformation(){
        try {
            ConversationService conversationService = new ConversationService();

            ConversationResponseDTO conversationInfo = conversationService.getConversationById(currentConversationId);

            isAdmin = conversationService.isAdmin(currentConversationId);

            boolean canAdd = isAdmin || conversationInfo.isAllowParticipantsInvite();

            addParticipantButton.setVisible(canAdd);
            addParticipantButton.setManaged(canAdd);

            allowInviteToggle.setVisible(isAdmin);
            allowInviteToggle.setManaged(isAdmin);
            allowInviteToggle.setSelected(conversationInfo.isAllowParticipantsInvite());

            groupPictureView.setText(conversationInfo.getGroupName().substring(0, 1));

            groupPictureView.setStyle("-fx-background-radius: 50%; -fx-text-fill: white; -fx-background-color: black");

            groupNameField.setText(conversationInfo.getGroupName());

            participantList.setCellFactory(_ -> new ParticipantCell(isAdmin,
                    participantId -> {
                        try {
                            new ConversationService().promoteToAdmin(currentConversationId, participantId);

                            loadGroupInformation();
                        } catch (Exception _) {
                            AlertUtils.showError(errorLabel, "Couldn't promote participant");
                        }
                    },
                    participantId -> {
                        try {
                            new ConversationService().demoteAdmin(currentConversationId, participantId);

                            loadGroupInformation();
                        } catch (Exception _) {
                            AlertUtils.showError(errorLabel, "Couldn't demote participant");
                        }
                    },
                    participantId -> {
                        try {
                            new ConversationService().kickParticipant(currentConversationId, participantId);

                            loadGroupInformation();
                        } catch (Exception _) {
                            AlertUtils.showError(errorLabel, "Couldn't kick participant");
                        }
                    }
            ));

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
        try {
            AddParticipantsController controller = SceneManager.switchContent(contentPane, "add-participants-page.fxml");

            controller.setConversationId(currentConversationId);

            controller.loadFriendList();

            controller.setOnBack(() -> {
                try {
                    GroupInfoController groupInfoController = SceneManager.switchContent(contentPane, "group-info-page.fxml");

                    groupInfoController.setCurrentConversationId(currentConversationId);

                    groupInfoController.setOnBack(onBack);

                    groupInfoController.loadGroupInformation();
                } catch (Exception _) {
                    AlertUtils.showError(errorLabel, "Couldn't go back");
                }
            });
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load add participants page");
        }
    }

    @FXML
    public void handleSave(){
        String groupName = groupNameField.getText().trim();
        String profilePictureName = groupPictureView.getText().trim();

        if(groupName.isBlank()){
            AlertUtils.showError(errorLabel, "Group name cannot be empty");

            return;
        }

        try {
            ConversationService conversationService = new ConversationService();

            conversationService.updateGroupInfo(currentConversationId, groupName, profilePictureName);

            onGroupUpdated.accept(currentConversationId, groupName, profilePictureName);
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't save changes");
        }
    }

    public void handleToggleInvite(){
        try {
            ConversationService conversationService = new ConversationService();

            boolean allowed = allowInviteToggle.isSelected();

            if(isAdmin){
                conversationService.allowParticipantsInvite(currentConversationId, allowed);
            }else{
                AlertUtils.showError(errorLabel, "You are not an admin");
            }
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't change toggle");
        }
    }
}