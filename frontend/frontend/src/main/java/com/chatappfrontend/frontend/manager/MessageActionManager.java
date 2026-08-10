package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.MessageResponseDTO;
import com.chatappfrontend.frontend.service.MessageService;

import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lombok.Setter;

import java.util.function.Consumer;

public class MessageActionManager {
    private final MessageEventManager messageEventManager;
    private final ConversationListManager conversationListManager;
    private final Consumer<String> onError;
    private final VBox messagesContainer;

    @Setter
    private Long currentConversationId;

    public MessageActionManager(MessageEventManager messageEventManager, ConversationListManager conversationListManager, Consumer<String> onError, VBox messagesContainer) {
        this.messageEventManager = messageEventManager;
        this.conversationListManager = conversationListManager;
        this.onError = onError;
        this.messagesContainer = messagesContainer;
    }

    public void handleEdit(MessageResponseDTO message){
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

                MessageResponseDTO editedMessage = messageService.editMessage(message.getId(), trimmed);

                message.setMessage(editedMessage.getMessage());

                messageEventManager.refreshMessageBubble(message);

                if(messageEventManager.isLastMessageInContainer(message.getId())){
                    conversationListManager.updateConversationPreview(currentConversationId, editedMessage.getMessage(), editedMessage.getSentAt());
                }
            } catch (Exception _) {
                onError.accept("Couldn't edit the message");
            }
        });
    }

    public void handleDeleteForMe(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForMe(message.getId());
            } catch (Exception _) {
                onError.accept("Couldn't delete the message");
            }
        });
    }

    public void handleDeleteForEveryone(MessageResponseDTO message, HBox bubble){
        deleteAndSync(message, bubble, () -> {
            try {
                new MessageService().deleteMessageForEveryone(message.getId());
            } catch (Exception _) {
                onError.accept("Couldn't delete the message");
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
            onError.accept("Couldn't delete the message");
        }
    }
}