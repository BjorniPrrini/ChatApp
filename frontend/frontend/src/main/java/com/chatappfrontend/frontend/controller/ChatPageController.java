package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.ConversationDTO;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class ChatPageController {
    @FXML
    private Label notificationLabel;
    @FXML
    private Pane backgroundPane;
    @FXML
    private Button chatsIconButton;
    @FXML
    private Button settingsIconButton;
    @FXML
    private VBox fixedPanel;
    @FXML
    private VBox conversationsPanel;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<ConversationDTO> conversationList;
    @FXML
    private VBox settingsPanel;
    @FXML
    private HBox chatHeader;
    @FXML
    private Label chatNameLabel;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messagesContainer;
    @FXML
    private HBox messageInputArea;
    @FXML
    private TextField messageInput;

    @FXML
    public void initialize(){
        conversationList.setCellFactory(_ -> new ConversationCell());

        loadConversations();
    }

    @FXML
    public void showConversations(){
        conversationsPanel.setVisible(true);
        conversationsPanel.setManaged(true);
        settingsPanel.setVisible(false);
        settingsPanel.setManaged(false);
    }

    @FXML
    public void showSettings(){
        settingsPanel.setVisible(true);
        settingsPanel.setManaged(true);
        conversationsPanel.setVisible(false);
        conversationsPanel.setManaged(false);
    }

    @FXML
    public void handleLogout(){
        SessionManager.getInstance().clear();

        try {
            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            showError("Can't load loading page");
        }
    }

    private void loadConversations(){
        try {
            ConversationService service = new ConversationService();

            List<ConversationDTO> conversations = service.getConversations();

            conversationList.getItems().clear();
            conversationList.getItems().addAll(conversations);
        } catch (Exception e) {
            showError("Failed to load conversations");
        }
    }

    @FXML
    public void handleChangePassword(){
    }

    @FXML
    public void handleEditProfile(){
    }

    @FXML
    public void handleProfilePicture(){
    }

    @FXML
    public void handleSendMessage(){
    }

    private void showError(String message){
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        pause.setOnFinished(_ -> {
            notificationLabel.setVisible(false);
            notificationLabel.setManaged(false);
        });

        pause.play();
    }
}