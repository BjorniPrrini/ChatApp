package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.*;
import com.chatappfrontend.frontend.service.*;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatPageController {
    @FXML
    private Button friendsIconButton;
    @FXML
    private VBox friendsPanel;
    @FXML
    private TextField friendSearchField;
    @FXML
    private ListView<UserResponseDTO> searchResultsList;
    @FXML
    private ListView<FriendResponseDTO> friendRequestsList;
    @FXML
    private ListView<FriendResponseDTO> friendsList;
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
    private final WebSocketService webSocketService = new WebSocketService();
    private Set<Long> friendIds = new HashSet<>();
    private Set<Long> pendingIds = new HashSet<>();

    @FXML
    public void initialize(){
        conversationList.setCellFactory(_ -> new ConversationCell());

        searchResultsList.setCellFactory(_ -> new UserCell(friendIds, pendingIds));

        loadConversations();

        conversationList.setOnMouseClicked(_ -> {
            ConversationResponseDTO selected = conversationList.getSelectionModel().getSelectedItem();

            if(selected != null){
                openConversation(selected);
            }
        });

        friendRequestsList.setCellFactory(_ -> new FriendRequestCell(() -> {
            loadFriendRequests();
            loadFriends();
        }));

        friendsList.setCellFactory(_ -> new FriendsCell(otherUserId -> {
            try {
                ConversationService conversationService = new ConversationService();

                ConversationResponseDTO conversation = conversationService.createConversation(otherUserId);

                showPanel(conversationsPanel);
                openConversation(conversation);
            } catch (Exception e) {
                showError("Could not start conversation");
            }
        }));

        friendSearchField.setOnKeyPressed(event -> {
            if(event.getCode() == javafx.scene.input.KeyCode.ENTER){
                String term = friendSearchField.getText().trim();

                if(term.length() >= 2){
                    searchUsers(term);
                }
            }
        });

        try {
            webSocketService.connect();
        } catch (Exception e) {
            showError("Could not connect to real time service");
        }
    }

    private void searchUsers(String term){
        try {
            UserService userService = new UserService();

            List<UserResponseDTO> users = userService.searchUsers(term);

            users = users.stream()
                    .filter(u -> !u.getId().equals(SessionManager.getInstance().getUserId()))
                    .toList();

            searchResultsList.getItems().clear();
            searchResultsList.getItems().addAll(users);
        } catch (Exception e) {
            showError("User not found");
        }
    }

    private void openConversation(ConversationResponseDTO selected){
        currentConversationId = selected.getConversationId();

        webSocketService.unsubscribe();
        webSocketService.subscribe(currentConversationId, message -> {
            Platform.runLater(() -> {
                HBox bubble = createMessageBubble(message);

                messagesContainer.getChildren().add(bubble);
            });
        });

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
    public void showFriends(){
        try {
            FriendService friendService = new FriendService();

            friendIds.clear();
            pendingIds.clear();

            Long currentUserId = SessionManager.getInstance().getUserId();

            friendService.getFriends().forEach(f -> {
                Long friendId = f.getSenderId().equals(currentUserId) ? f.getReceiverId() : f.getSenderId();

                friendIds.add(friendId);
            });

            friendService.getSentRequests().forEach(f -> pendingIds.add(f.getReceiverId()));
        } catch (Exception e) {
            showError("Could not load friend status");
        }

        showPanel(friendsPanel);
        loadFriendRequests();
        loadFriends();
    }

    @FXML
    public void handleLogout(){
        webSocketService.disconnect();

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

    private void loadFriendRequests() {
        try {
            FriendService friendService = new FriendService();

            List<FriendResponseDTO> friendRequests = friendService.getFriendRequests();

            friendRequestsList.setCellFactory(_ -> new FriendRequestCell(() -> {
                loadFriendRequests();
                loadFriends();
            }));

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