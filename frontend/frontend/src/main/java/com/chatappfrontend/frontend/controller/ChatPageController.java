package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.cell.ConversationCell;
import com.chatappfrontend.frontend.cell.FriendRequestCell;
import com.chatappfrontend.frontend.cell.FriendsCell;
import com.chatappfrontend.frontend.cell.UserCell;
import com.chatappfrontend.frontend.model.*;
import com.chatappfrontend.frontend.service.*;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
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
    @FXML
    private VBox replyPreviewBox;

    private Long currentConversationId;
    private final WebSocketService webSocketService = new WebSocketService();
    private Set<Long> friendIds = new HashSet<>();
    private Set<Long> pendingIds = new HashSet<>();
    private MessageResponseDTO replyingTo;
    private LocalDateTime oldestLoadedMessageTime;
    private boolean hasMoreMessages = true;
    private boolean isLoadingMore = false;

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

        friendsList.setCellFactory(_ -> new FriendsCell(
                friendId -> {
                    try {
                        ConversationService conversationService = new ConversationService();

                        ConversationResponseDTO conversation = conversationService.createConversation(friendId);

                        showPanel(conversationsPanel);
                        openConversation(conversation);
                    } catch (Exception e) {
                        showError("Could not start conversation");
                    }
                },
                friendId -> {
                    try {
                        FriendService friendService = new FriendService();

                        friendService.removeFriend(SessionManager.getInstance().getUserId(), friendId);

                        showFriends();
                    } catch (Exception e) {
                        showError("Could not remove friend");
                    }
                },
                friendId -> {
                    try {
                        FriendService friendService = new FriendService();

                        friendService.blockFriend(friendId);

                        showFriends();
                    } catch (Exception e) {
                        showError("Could not block user");
                    }
                }
        ));

        friendSearchField.setOnKeyPressed(event -> {
            if(event.getCode() == javafx.scene.input.KeyCode.ENTER){
                String term = friendSearchField.getText().trim();

                if(term.length() >= 2){
                    searchUsers(term);
                }
            }
        });

        messageInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleSendMessage();
            }
        });

        messagesContainer.heightProperty().addListener((_, _, _) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        messagesScrollPane.vvalueProperty().addListener((_, _, newValue) -> {
            if(newValue.doubleValue() <= 0.05 && hasMoreMessages && !isLoadingMore){
                loadOlderMessages();
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

    private void updateConversationPreview(Long conversationId, String messageText, LocalDateTime sentAt){
        ObservableList<ConversationResponseDTO> items = conversationList.getItems();

        for(int i = 0; i < items.size(); i++){
            ConversationResponseDTO c = items.get(i);

            if(c.getConversationId().equals(conversationId)){
                c.setLastMessage(messageText);
                c.setLastMessageAt(sentAt);

                if(i != 0){
                    items.remove(i);
                    items.addFirst(c);
                }

                conversationList.refresh();

                return;
            }
        }

        loadConversations();
    }

    private void openConversation(ConversationResponseDTO selected){
        currentConversationId = selected.getConversationId();
        oldestLoadedMessageTime = null;
        hasMoreMessages = true;
        isLoadingMore = false;

        webSocketService.unsubscribe();
        webSocketService.subscribe(currentConversationId, event -> {
            Platform.runLater(() -> {
                switch(event.getType()){
                    case "NEW" -> {
                        MessageResponseDTO message = event.getMessage();

                        boolean alreadyShown = messagesContainer.getChildren().stream().anyMatch(node -> message.getId().equals(node.getProperties().get("messageId")));

                        if(!alreadyShown){
                            HBox bubble = createMessageBubble(message);

                            messagesContainer.getChildren().add(bubble);
                        }

                        updateConversationPreview(currentConversationId, message.getMessage(), message.getSentAt());
                    }
                    case "EDIT" -> {
                        MessageResponseDTO message = event.getMessage();

                        refreshMessageBubble(message);

                        if(isLastMessageInContainer(message.getId())){
                            updateConversationPreview(currentConversationId, message.getMessage(), message.getSentAt());
                        }
                    }
                    case "DELETE" -> {
                        boolean wasLast = isLastMessageInContainer(event.getMessageId());

                        messagesContainer.getChildren().removeIf(node -> event.getMessageId().equals(node.getProperties().get("messageId")));

                        if(wasLast){
                            syncPreviewToNewLastMessage();
                        }
                    }
                }
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

            if(!messages.isEmpty()){
                oldestLoadedMessageTime = messages.get(0).getSentAt();
            }

            hasMoreMessages = messagePage.isHasMore();
        } catch (Exception e){
            showError("Couldn't get the messages");
        }
    }

    private HBox createMessageBubble(MessageResponseDTO message){
        HBox hBox = new HBox();

        hBox.getProperties().put("messageId", message.getId());
        hBox.getProperties().put("messageObj", message);

        VBox bubble = new VBox();

        bubble.setSpacing(5);
        bubble.setMaxWidth(400);

        boolean isMyMessage = message.getSenderId().equals(SessionManager.getInstance().getUserId());

        if(message.getReplyToId() != null){
            Label replyLabel = new Label(message.getReplyToMessage());

            replyLabel.setWrapText(true);
            replyLabel.setMaxWidth(300);

            replyLabel.setStyle("-fx-background-color: #555555;" + "-fx-text-fill: #dddddd;" + "-fx-padding: 6 8;" + "-fx-background-radius: 8;" + "-fx-font-size: 12px;");

            bubble.getChildren().add(replyLabel);
        }

        Label messageLabel = new Label(message.getMessage());

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        if(isMyMessage){
            messageLabel.setStyle("-fx-background-color: #00ff88;" + "-fx-text-fill: black;" + "-fx-padding: 8 12;" + "-fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_RIGHT);
        }else{
            messageLabel.setStyle("-fx-background-color: #1a1a1a;" + "-fx-text-fill: white;" + "-fx-padding: 8 12;" + "-fx-background-radius: 15;");

            hBox.setAlignment(Pos.CENTER_LEFT);
        }

        bubble.getChildren().add(messageLabel);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem reply = new MenuItem("Reply");

        reply.setOnAction(_ -> handleReply(message));

        contextMenu.getItems().add(reply);

        if(isMyMessage){
            MenuItem edit = new MenuItem("Edit");

            edit.setOnAction(_ -> handleEdit(message));

            MenuItem deleteForMe = new MenuItem("Delete for me");

            deleteForMe.setOnAction(_ -> handleDeleteForMe(message, hBox));

            MenuItem deleteForEveryone = new MenuItem("Delete for everyone");

            deleteForEveryone.setOnAction(_ -> handleDeleteForEveryone(message, hBox));

            contextMenu.getItems().addAll(edit, deleteForMe, deleteForEveryone);
        }else{
            MenuItem deleteForMe = new MenuItem("Delete for me");

            deleteForMe.setOnAction(_ -> handleDeleteForMe(message, hBox));

            contextMenu.getItems().add(deleteForMe);
        }

        messageLabel.setContextMenu(contextMenu);

        hBox.getChildren().add(bubble);

        return hBox;
    }

    private void handleReply(MessageResponseDTO message){
        replyingTo = message;

        replyPreviewBox.getChildren().clear();

        Label replyLabel = new Label("Replying to: " + message.getMessage());

        replyLabel.setWrapText(true);
        replyLabel.setMaxWidth(350);

        replyLabel.setStyle("-fx-background-color: #333333;" + "-fx-text-fill: white;" + "-fx-padding: 8;" + "-fx-background-radius: 8;");

        Button cancelButton = new Button("X");

        cancelButton.setOnAction(e -> cancelReply());

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

                refreshMessageBubble(message);

                if(isLastMessageInContainer(message.getId())){
                    updateConversationPreview(currentConversationId, edited.getMessage(), edited.getSentAt());
                }
            } catch (Exception e) {
                showError("Couldn't edit message");
            }
        });
    }

    private void handleDeleteForMe(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForMe(message.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleDeleteForEveryone(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForEveryone(message.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void deleteAndSync(MessageResponseDTO message, HBox bubble, Runnable deleteCall){
        try {
            boolean wasLast = isLastMessageInContainer(message.getId());

            deleteCall.run();

            messagesContainer.getChildren().remove(bubble);

            if(wasLast){
                syncPreviewToNewLastMessage();
            }
        } catch (Exception e) {
            showError("Couldn't delete message");
        }
    }

    private boolean isLastMessageInContainer(Long messageId){
        if(messagesContainer.getChildren().isEmpty()){
            return false;
        }

        Node lastNode = messagesContainer.getChildren().get(messagesContainer.getChildren().size() - 1);

        return messageId.equals(lastNode.getProperties().get("messageId"));
    }

    private void syncPreviewToNewLastMessage(){
        if(messagesContainer.getChildren().isEmpty()){
            updateConversationPreview(currentConversationId, "", null);

            return;
        }

        Node lastNode = messagesContainer.getChildren().get(messagesContainer.getChildren().size() - 1);

        MessageResponseDTO lastMessage = (MessageResponseDTO) lastNode.getProperties().get("messageObj");

        if(lastMessage != null){
            updateConversationPreview(currentConversationId, lastMessage.getMessage(), lastMessage.getSentAt());
        }
    }

    private void refreshMessageBubble(MessageResponseDTO message){
        for(int i = 0; i < messagesContainer.getChildren().size(); i++){
            Node node = messagesContainer.getChildren().get(i);

            if(message.getId().equals(node.getProperties().get("messageId"))){
                HBox newBubble = createMessageBubble(message);

                messagesContainer.getChildren().set(i, newBubble);

                return;
            }
        }
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

            conversations.sort((a, b) -> {
                if(a.getLastMessageAt() == null && b.getLastMessageAt() == null){
                    return 0;
                }

                if(a.getLastMessageAt() == null){
                    return 1;
                }

                if(b.getLastMessageAt() == null){
                    return -1;
                }

                return b.getLastMessageAt().compareTo(a.getLastMessageAt());
            });

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

            MessageResponseDTO sent;

            if(replyingTo != null){
                sent = messageService.replyMessage(currentConversationId, replyingTo.getId(), message);
            }else{
                sent = messageService.sendMessage(currentConversationId, message);
            }

            HBox bubble = createMessageBubble(sent);

            messagesContainer.getChildren().add(bubble);

            updateConversationPreview(currentConversationId, sent.getMessage(), sent.getSentAt());

            messageInput.clear();

            cancelReply();

        } catch (Exception e){
            showError("Couldn't send message");
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
                HBox bubble = createMessageBubble(olderMessages.get(i));

                messagesContainer.getChildren().add(i, bubble);
            }

            oldestLoadedMessageTime = olderMessages.get(0).getSentAt();
            hasMoreMessages = messagePage.isHasMore();

            Platform.runLater(() -> {
                double heightAfter = messagesContainer.getHeight();
                double addedHeight = heightAfter - heightBefore;

                double currentVvalue = messagesScrollPane.getVvalue();
                double totalHeight = messagesContainer.getHeight() - messagesScrollPane.getViewportBounds().getHeight();

                if(totalHeight > 0){
                    double currentPixelOffset = currentVvalue * (totalHeight - addedHeight);
                    double newVvalue = (currentPixelOffset + addedHeight) / totalHeight;

                    messagesScrollPane.setVvalue(newVvalue);
                }

                isLoadingMore = false;
            });
        } catch (Exception e) {
            showError("Couldn't load older messages");
            isLoadingMore = false;
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