package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.service.ConversationService;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class ConversationListManager {
    private final ListView<ConversationResponseDTO> conversationList;
    private final Consumer<String> onError;
    private final ConversationService conversationService = new ConversationService();

    public ConversationListManager(ListView<ConversationResponseDTO> conversationList, Consumer<String> onError) {
        this.conversationList = conversationList;
        this.onError = onError;
    }

    public void loadConversations(){
        try {
            List<ConversationResponseDTO> conversations = conversationService.getConversations();

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
            for(ParticipantDTO participant : c.getParticipants()){
                if(participant.getUserId().equals(userId)){
                    participant.setOnline(isOnline);

                    break;
                }
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

    public void updateConversationGroupInfo(Long conversationId, String newName, String profilePicture){
        if(newName == null && profilePicture == null){
            return;
        }

        for(ConversationResponseDTO conversation : conversationList.getItems()){
            if(conversation.getConversationId().equals(conversationId)){
                if((newName == null || conversation.getGroupName().equals(newName)) && (profilePicture == null || conversation.getGroupPicture().equals(profilePicture))){
                    return;
                }

                if(newName != null){
                    conversation.setGroupName(newName);
                }

                if(profilePicture != null){
                    conversation.setGroupPicture(profilePicture);
                }

                conversationList.refresh();

                break;
            }
        }
    }
}