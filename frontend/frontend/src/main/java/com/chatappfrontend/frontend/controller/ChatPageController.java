package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.*;
import com.chatappfrontend.frontend.factory.MessageBubbleFactory;
import com.chatappfrontend.frontend.manager.ConversationListManager;
import com.chatappfrontend.frontend.manager.MessageEventManager;
import com.chatappfrontend.frontend.manager.PanelManager;
import com.chatappfrontend.frontend.manager.WebSocketConnectionManager;
import com.chatappfrontend.frontend.model.*;
import com.chatappfrontend.frontend.service.*;
import com.chatappfrontend.frontend.util.AlertUtils;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ChatPageController {
    @FXML
    private Button friendsIconButton;
    @FXML
    private VBox friendsPanel;
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
    @FXML
    private VBox replyPreviewBox;
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox chatArea;

    private Long currentConversationId;
    private final WebSocketService webSocketService = new WebSocketService();
    private MessageResponseDTO replyingTo;
    private LocalDateTime oldestLoadedMessageTime;
    private boolean hasMoreMessages = true;
    private boolean isLoadingMore = false;
    private MessageBubbleFactory messageBubbleFactory;
    private ConversationListManager conversationListManager;
    private PanelManager panelManager;
    private WebSocketConnectionManager webSocketConnectionManager;
    private MessageEventManager messageEventManager;

    @FXML
    public void initialize(){
        conversationList.setCellFactory(_ -> new ConversationCell(conversationId -> {
            conversationListManager.removeConversation(conversationId);

            if(currentConversationId.equals(conversationId)){
                clearChatPane();
            }
        }));

        messageBubbleFactory = new MessageBubbleFactory(SessionManager.getInstance().getUserId(), this::handleReply, this::handleEdit, this::handleDeleteForMe, this::handleDeleteForEveryone);

        conversationListManager = new ConversationListManager(conversationList, message -> AlertUtils.showError(notificationLabel, message));

        messageEventManager = new MessageEventManager(messagesContainer,messageBubbleFactory, conversationListManager, webSocketService);

        panelManager = new PanelManager(List.of(conversationsPanel, settingsPanel, friendsPanel));

        webSocketConnectionManager = new WebSocketConnectionManager(webSocketService);

        conversationListManager.loadConversations();

        conversationList.setOnMouseClicked(_ -> {
            ConversationResponseDTO selected = conversationList.getSelectionModel().getSelectedItem();

            if(selected != null){
                openConversation(selected);
            }
        });

        messageInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleSendMessage();
            }
        });

        messagesContainer.heightProperty().addListener((_, _, _) -> messagesScrollPane.setVvalue(1.0));

        messagesScrollPane.vvalueProperty().addListener((_, _, newValue) -> {
            if(newValue.doubleValue() <= 0.05 && hasMoreMessages && !isLoadingMore){
                loadOlderMessages();
            }
        });

        try {
            webSocketConnectionManager.connect(SessionManager.getInstance().getUserId(), messageEventManager::handleUserQueueEvent, messageEventManager::handleUserStatusEvent);
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Could not connect to real time service");
        }
    }

    private void openConversation(ConversationResponseDTO selected){
        showChatContent();

        currentConversationId = selected.getConversationId();
        messageEventManager.setCurrentConversationId(currentConversationId);
        oldestLoadedMessageTime = null;
        hasMoreMessages = true;
        isLoadingMore = false;

        webSocketConnectionManager.subscribeToConversation(currentConversationId, messageEventManager::handleConversationEvent);

        chatNameLabel.setText(selected.getNickname() != null ? selected.getNickname() : selected.getName() + " " + selected.getSurname());

        messagesContainer.getChildren().clear();

        try {
            MessageService messageService = new MessageService();

            MessagePageDTO messagePage = messageService.getMessages(currentConversationId, null);

            List<MessageResponseDTO> messages = messagePage.getMessages();

            for(MessageResponseDTO message : messages){
                HBox bubble = messageBubbleFactory.createMessageBubble(message);

                messagesContainer.getChildren().add(bubble);
            }

            if(!messages.isEmpty()){
                oldestLoadedMessageTime = messages.getFirst().getSentAt();
            }

            hasMoreMessages = messagePage.isHasMore();

            messageInput.requestFocus();
        } catch (Exception _){
            AlertUtils.showError(notificationLabel, "Couldn't get the messages");
        }
    }

    private void handleReply(MessageResponseDTO message){
        replyingTo = message;

        replyPreviewBox.getChildren().clear();

        Label replyLabel = new Label("Replying to: " + message.getMessage());

        replyLabel.setWrapText(true);
        replyLabel.setMaxWidth(350);

        replyLabel.setStyle("-fx-background-color: #333333;" + "-fx-text-fill: white;" + "-fx-padding: 8;" + "-fx-background-radius: 8;");

        Button cancelButton = new Button("X");

        cancelButton.setOnAction(_ -> cancelReply());

        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);

        preview.getChildren().addAll(replyLabel, cancelButton);

        replyPreviewBox.getChildren().add(preview);

        replyPreviewBox.setVisible(true);
        replyPreviewBox.setManaged(true);

        messageInput.requestFocus();
    }

    private void cancelReply(){
        replyingTo = null;

        replyPreviewBox.getChildren().clear();

        replyPreviewBox.setVisible(false);
        replyPreviewBox.setManaged(false);
    }

    private void handleEdit(MessageResponseDTO message){
        TextInputDialog textInputDialog = new TextInputDialog(message.getMessage());

        textInputDialog.setTitle("Edit message");
        textInputDialog.setHeaderText(null);
        textInputDialog.setContentText("Edit your message:");

        textInputDialog.showAndWait().ifPresent(newText -> {
            String trimmed = newText.trim();

            if(trimmed.isEmpty() || trimmed.equals(message.getMessage())){
                return;
            }

            try {
                MessageService messageService = new MessageService();

                MessageResponseDTO edited = messageService.editMessage(message.getId(), trimmed);

                message.setMessage(edited.getMessage());

                messageEventManager.refreshMessageBubble(message);

                if(messageEventManager.isLastMessageInContainer(message.getId())){
                    conversationListManager.updateConversationPreview(currentConversationId, edited.getMessage(), edited.getSentAt());
                }
            } catch (Exception _) {
                AlertUtils.showError(notificationLabel, "Couldn't edit the message");
            }
        });
    }

    private void handleDeleteForMe(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForMe(message.getId());
            } catch (Exception _) {
                AlertUtils.showError(notificationLabel, "Couldn't delete the message");
            }
        });
    }

    private void handleDeleteForEveryone(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForEveryone(message.getId());
            } catch (Exception _) {
                AlertUtils.showError(notificationLabel, "Couldn't delete the message");
            }
        });
    }

    private void deleteAndSync(MessageResponseDTO message, HBox bubble, Runnable deleteCall){
        try {
            boolean wasLast = messageEventManager.isLastMessageInContainer(message.getId());

            deleteCall.run();

            messagesContainer.getChildren().remove(bubble);

            if(wasLast){
                messageEventManager.syncPreviewToNewLastMessage();
            }
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't delete the message");
        }
    }

    private void showChatContent(){
        contentPane.getChildren().setAll(chatArea);
    }

    @FXML
    public void showConversations(){
        showChatContent();

        panelManager.showPanel(conversationsPanel);
    }

    @FXML
    public void showSettings(){
        panelManager.showPanel(settingsPanel);
    }

    @FXML
    public void showFriends(){
        panelManager.showPanel(friendsPanel);
    }

    @FXML
    public void handleLogout(){
        webSocketService.disconnect();

        SessionManager.getInstance().clear();

        try {
            SceneManager.switchTo("login-page.fxml");
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load loading page");
        }
    }

    @FXML
    public void handleChangePassword(){
        try {
            SceneManager.switchContent(contentPane, "change-password.fxml");
        } catch (IOException _) {
            AlertUtils.showError(notificationLabel, "Couldn't load change password page");
        }
    }

    @FXML
    public void handleEditProfile(){
        try {
            SceneManager.switchContent(contentPane, "edit-profile.fxml");
        } catch (IOException _) {
            AlertUtils.showError(notificationLabel, "Couldn't load edit profile page");
        }
    }

    @FXML
    public void handleProfilePicture(){

    }

    @FXML
    public void handleSendMessage(){
        String message = messageInput.getText().trim();

        if(message.isEmpty()){
            AlertUtils.showError(notificationLabel, "Can't send empty messages");

            return;
        }

        if(currentConversationId == null){
            AlertUtils.showError(notificationLabel, "Not a valid conversation");

            return;
        }

        try {
            MessageService messageService = new MessageService();

            MessageResponseDTO sent;

            if(replyingTo != null){
                sent = messageService.replyMessage(currentConversationId, replyingTo.getId(), message);
            }else{
                sent = messageService.sendMessage(currentConversationId, message);
            }

            HBox bubble = messageBubbleFactory.createMessageBubble(sent);

            messagesContainer.getChildren().add(bubble);

            conversationListManager.updateConversationPreview(currentConversationId, sent.getMessage(), sent.getSentAt());

            messageInput.clear();

            cancelReply();

        } catch (Exception _){
            AlertUtils.showError(notificationLabel, "Couldn't send message");
        }
    }

    private void loadOlderMessages(){
        if(currentConversationId == null || oldestLoadedMessageTime == null){
            return;
        }

        isLoadingMore = true;

        try {
            MessageService messageService = new MessageService();

            MessagePageDTO messagePage = messageService.getMessages(currentConversationId, oldestLoadedMessageTime);

            List<MessageResponseDTO> olderMessages = messagePage.getMessages();

            if(olderMessages.isEmpty()){
                hasMoreMessages = false;
                isLoadingMore = false;

                return;
            }

            double heightBefore = messagesContainer.getHeight();

            for(int i = 0; i < olderMessages.size(); i++){
                HBox bubble = messageBubbleFactory.createMessageBubble(olderMessages.get(i));

                messagesContainer.getChildren().add(i, bubble);
            }

            oldestLoadedMessageTime = olderMessages.getFirst().getSentAt();
            hasMoreMessages = messagePage.isHasMore();

            Platform.runLater(() -> {
                double heightAfter = messagesContainer.getHeight();
                double addedHeight = heightAfter - heightBefore;

                double currentValue = messagesScrollPane.getVvalue();
                double totalHeight = messagesContainer.getHeight() - messagesScrollPane.getViewportBounds().getHeight();

                if(totalHeight > 0){
                    double currentPixelOffset = currentValue * (totalHeight - addedHeight);
                    double newValue = (currentPixelOffset + addedHeight) / totalHeight;

                    messagesScrollPane.setVvalue(newValue);
                }

                isLoadingMore = false;
            });
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load older messages");

            isLoadingMore = false;
        }
    }

    private void clearChatPane(){
        currentConversationId = null;
        messageEventManager.setCurrentConversationId(null);

        messagesContainer.getChildren().clear();

        chatNameLabel.setText("");

        webSocketService.unsubscribe();
    }

    @FXML
    public void handleAddFriends(){
        try {
            SceneManager.switchContent(contentPane, "add-friend-page.fxml");
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load add friend page");
        }
    }

    @FXML
    public void handleFriendRequests(){
        try {
            SceneManager.switchContent(contentPane, "friend-requests-page.fxml");
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load friend requests page");
        }
    }

    @FXML
    public void handleBlockedUsers(){
        try {
            SceneManager.switchContent(contentPane, "blocked-users-page.fxml");
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load blocked users page");
        }
    }

    @FXML
    public void handleSuggestedFriends(){
        try {
            SceneManager.switchContent(contentPane, "suggested-users-page.fxml");
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load suggested friends page");
        }
    }

    @FXML
    public void handleMyFriends(){
        try {
            MyFriendsController controller = SceneManager.switchContent(contentPane, "my-friends-page.fxml");

            controller.setOnStartConversation(conversation -> {
                panelManager.showPanel(conversationsPanel);

                openConversation(conversation);
            });
        } catch (Exception _) {
            AlertUtils.showError(notificationLabel, "Couldn't load friends page");
        }
    }
}