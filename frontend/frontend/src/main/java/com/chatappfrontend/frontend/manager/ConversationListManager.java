package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.service.ConversationService;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class ConversationListManager {
    private final ListView<ConversationResponseDTO> conversationList;
    private final Consumer<String> onError;

    public ConversationListManager(ListView<ConversationResponseDTO> conversationList, Consumer<String> onError) {
        this.conversationList = conversationList;
        this.onError = onError;
    }

    public void loadConversations(){
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
        } catch (Exception _) {
            onError.accept("Failed to load conversations");
        }
    }

    public void updateConversationPreview(Long conversationId, String messageText, LocalDateTime sentAt){
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

    public void updateFriendStatus(Long userId, String status){
        boolean isOnline = status.equals("online");

        for(ConversationResponseDTO c : conversationList.getItems()){
            if(c.getOtherUserId().equals(userId)){
                c.setOnline(isOnline);

                break;
            }
        }

        conversationList.refresh();
    }

    public void removeConversation(Long conversationId){
        try {
            ConversationService service = new ConversationService();

            service.deleteConversation(conversationId);

            ObservableList<ConversationResponseDTO> items = conversationList.getItems();

            for(int i = 0; i < items.size(); i++){
                ConversationResponseDTO c = items.get(i);

                if(c.getConversationId().equals(conversationId)){
                    conversationList.getItems().remove(i);

                    return;
                }
            }
        } catch (Exception _) {
            onError.accept("Failed to delete conversation");
        }
    }
}