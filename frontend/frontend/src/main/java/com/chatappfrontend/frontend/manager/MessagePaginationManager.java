package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.factory.MessageBubbleFactory;
import com.chatappfrontend.frontend.model.MessagePageDTO;
import com.chatappfrontend.frontend.model.MessageResponseDTO;
import com.chatappfrontend.frontend.service.MessageService;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class MessagePaginationManager {
    private final VBox messagesContainer;
    private final MessageBubbleFactory messageBubbleFactory;
    private final ScrollPane scrollPane;
    private final Consumer<String> onError;

    private LocalDateTime oldestLoadedMessageTime;
    private boolean hasMoreMessages = true;
    private boolean isLoadingMore = false;

    @Setter
    private Long currentConversationId;

    public MessagePaginationManager(VBox messagesContainer, MessageBubbleFactory messageBubbleFactory, ScrollPane scrollPane, Consumer<String> onError) {
        this.messagesContainer = messagesContainer;
        this.messageBubbleFactory = messageBubbleFactory;
        this.scrollPane = scrollPane;
        this.onError = onError;

        scrollPane.vvalueProperty().addListener((_, _, newValue) -> {
            if(newValue.doubleValue() <= 0.05 && hasMoreMessages && !isLoadingMore){
                loadOlderMessages();
            }
        });
    }

    public void resetPagination(){
        oldestLoadedMessageTime = null;
        hasMoreMessages = true;
        isLoadingMore = false;
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
                double currentValue = scrollPane.getVvalue();
                double totalHeight = messagesContainer.getHeight() - scrollPane.getViewportBounds().getHeight();

                if(totalHeight > 0){
                    double currentPixelOffset = currentValue * (totalHeight - addedHeight);
                    double newValue = (currentPixelOffset + addedHeight) / totalHeight;

                    scrollPane.setVvalue(newValue);
                }

                isLoadingMore = false;
            });
        } catch (Exception _) {
            onError.accept("Couldn't load older messages");

            isLoadingMore = false;
        }
    }

    public void setInitialMessages(MessagePageDTO messagePage){
        List<MessageResponseDTO> messages = messagePage.getMessages();

        if(!messages.isEmpty()){
            oldestLoadedMessageTime = messages.getFirst().getSentAt();
        }else{
            oldestLoadedMessageTime = null;
        }

        hasMoreMessages = messagePage.isHasMore();
        isLoadingMore = false;
    }
}