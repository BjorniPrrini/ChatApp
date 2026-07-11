package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.*;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.service.FriendService;
import com.chatappfrontend.frontend.service.MessageService;
import com.chatappfrontend.frontend.service.UserService;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class ChatPageController {
    @FXML
    public Button friendsIconButton;
    @FXML
    public VBox friendsPanel;
    @FXML
    public TextField friendSearchField;
    @FXML
    public ListView<UserResponseDTO> searchResultsList;
    @FXML
    public ListView<FriendResponseDTO> friendRequestsList;
    @FXML
    public ListView<FriendResponseDTO> friendsList;
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
    private ListView<ConversationResponseDTO> conversationList;
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

    private Long currentConversationId;

    @FXML
    public void initialize(){
        conversationList.setCellFactory(_ -> new ConversationCell());

        loadConversations();

        conversationList.setOnMouseClicked(_ -> {
            ConversationResponseDTO selected = conversationList.getSelectionModel().getSelectedItem();

            if(selected != null){
                openConversation(selected);
            }
        });

        friendRequestsList.setCellFactory(_ -> new FriendRequestCell());
        friendsList.setCellFactory(_ -> new FriendRequestCell());

        friendSearchField.textProperty().addListener((_, _, newValue) -> {
            if(newValue.length() >= 2){
                searchUsers(newValue);
            }else{
                searchResultsList.getItems().clear();
            }
        });
    }

    private void searchUsers(String term){
        try {
            UserService userService = new UserService();

            List<UserResponseDTO> users = userService.searchUsers(term);

            searchResultsList.setCellFactory(_ -> new UserCell());

            searchResultsList.getItems().clear();
            searchResultsList.getItems().addAll(users);
        } catch (Exception e) {
            showError("No users found");
        }
    }

    private void openConversation(ConversationResponseDTO selected){
        currentConversationId = selected.getConversationId();

        chatNameLabel.setText(selected.getNickname() != null ? selected.getNickname() : selected.getName() + " " + selected.getSurname());

        messagesContainer.getChildren().clear();

        try {
            MessageService messageService = new MessageService();

            MessagePageDTO messagePage = messageService.getMessages(currentConversationId, null);

            List<MessageResponseDTO> messages = messagePage.getMessages();

            for(MessageResponseDTO message : messages){
                HBox bubble = createMessageBubble(message);

                messagesContainer.getChildren().add(bubble);
            }
        } catch (Exception e){
            showError("Couldn't get the messages");
        }
    }

    private HBox createMessageBubble(MessageResponseDTO message){
        HBox hBox = new HBox();
        Label label = new Label(message.getMessage());

        if(message.getSenderId().equals(SessionManager.getInstance().getUserId())){
            label.setStyle("-fx-background-color: #00ff88; -fx-text-fill: #000000; -fx-padding: 8 12; -fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_RIGHT);
        }else{
            label.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff; -fx-padding: 8 12; -fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_LEFT);
        }

        hBox.getChildren().add(label);

        return hBox;
    }

    @FXML
    public void showConversations(){
        showPanel(conversationsPanel);
    }

    @FXML
    public void showSettings(){
        showPanel(settingsPanel);
    }

    @FXML
    public void showFriends() {
        showPanel(friendsPanel);
        loadFriendRequests();
        loadFriends();
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

            List<ConversationResponseDTO> conversations = service.getConversations();

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

    public void loadFriendRequests(){
        try {
            FriendService friendService = new FriendService();

            List<FriendResponseDTO> friendRequests = friendService.getFriendRequests();

            friendRequestsList.getItems().clear();
            friendRequestsList.getItems().addAll(friendRequests);
        } catch (Exception e) {
            showError("Couldn't get friend requests");
        }
    }

    public void loadFriends(){
        try {
            FriendService friendService = new FriendService();

            List<FriendResponseDTO> friends = friendService.getFriends();

            friendsList.getItems().clear();
            friendsList.getItems().addAll(friends);
        } catch (Exception e) {
            showError("Couldn't get friends");
        }
    }

    @FXML
    public void handleSendMessage(){
        String message = messageInput.getText().trim();

        if(message.isEmpty()){
            showError("Can't send empty message");

            return;
        }

        if(currentConversationId == null){
            showError("Not a valid conversation");

            return;
        }

        try {
            MessageService messageService = new MessageService();

            messageService.sendMessage(currentConversationId, message);

            MessageResponseDTO newMessage = new MessageResponseDTO();

            newMessage.setSenderId(SessionManager.getInstance().getUserId());
            newMessage.setMessage(message);

            HBox bubble = createMessageBubble(newMessage);
            messagesContainer.getChildren().add(bubble);

            messageInput.clear();
        } catch (Exception e) {
            showError("Couldn't send message");
        }
    }

    private void hideAllPanels(){
        conversationsPanel.setVisible(false);
        conversationsPanel.setManaged(false);
        settingsPanel.setVisible(false);
        settingsPanel.setManaged(false);
        friendsPanel.setVisible(false);
        friendsPanel.setManaged(false);
    }

    private void showPanel(VBox panel){
        hideAllPanels();

        panel.setVisible(true);
        panel.setManaged(true);
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