package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.ParticipantDTO;
import com.chatappfrontend.frontend.model.ui.SearchResultItem;
import com.chatappfrontend.frontend.service.ConversationService;
import com.chatappfrontend.frontend.util.AppExecutor;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ConversationListManager {
    private final ListView<SearchResultItem> conversationList;
    private final Consumer<String> onError;
    private final ConversationService conversationService = new ConversationService();

    public ConversationListManager(ListView<SearchResultItem> conversationList, Consumer<String> onError) {
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

            List<SearchResultItem> wrapped = conversations.stream()
                            .map(SearchResultItem.ConversationResult::new)
                                    .map(item -> (SearchResultItem) item)
                                            .toList();

            conversationList.getItems().clear();
            conversationList.getItems().addAll(wrapped);
        } catch (Exception _) {
            onError.accept("Failed to load conversations");
        }
    }

    public void updateConversationPreview(Long conversationId, String messageText, LocalDateTime sentAt){
        for(SearchResultItem item : conversationList.getItems()){
            if(item instanceof SearchResultItem.ConversationResult(ConversationResponseDTO conversation) && conversation.getConversationId().equals(conversationId)){
                conversation.setLastMessage(messageText);
                conversation.setLastMessageAt(sentAt);

                conversationList.getItems().remove(item);
                conversationList.getItems().addFirst(item);

                conversationList.refresh();

                return;
            }
        }

        loadConversations();
    }

    public void updateFriendStatus(Long userId, String status){
        boolean isOnline = status.equals("online");

        for(ConversationResponseDTO c : getConversations()){
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
            conversationService.deleteConversation(conversationId);

            conversationList.getItems().removeIf(item -> item instanceof SearchResultItem.ConversationResult(ConversationResponseDTO conversation) && conversation.getConversationId().equals(conversationId));
        } catch (Exception _) {
            onError.accept("Failed to delete conversation");
        }
    }

    public void updateConversationGroupInfo(Long conversationId, String newName, String profilePicture){
        if(newName == null && profilePicture == null){
            return;
        }

        for(ConversationResponseDTO conversation : getConversations()){
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

    public void openOrFetchConversation(Long conversationId, Consumer<ConversationResponseDTO> onFound){
        for(SearchResultItem c : conversationList.getItems()){
            if(c instanceof SearchResultItem.ConversationResult(ConversationResponseDTO conversation) && conversation.getConversationId().equals(conversationId)){
                onFound.accept(conversation);

                return;
            }
        }

        Task<ConversationResponseDTO> fetchTask = new Task<>() {
            @Override
            protected ConversationResponseDTO call() throws Exception {
                return conversationService.getConversationById(conversationId);
            }
        };

        fetchTask.setOnSucceeded(_ -> onFound.accept(fetchTask.getValue()));

        fetchTask.setOnFailed(_ -> onError.accept("Failed search"));

        AppExecutor.run(fetchTask);
    }

    public void filter(String searchTerm){
        Iterator<SearchResultItem> iterator = conversationList.getItems().iterator();

        String searchTermLowerCase = searchTerm.toLowerCase(Locale.ROOT);

        while(iterator.hasNext()){
            SearchResultItem item = iterator.next();

            if(item instanceof SearchResultItem.ConversationResult(ConversationResponseDTO conversation)){
                String displayName = conversation.isGroup() ? conversation.getGroupName() : conversation.getParticipants().getFirst().getName() + " " + conversation.getParticipants().getFirst().getSurname();

                if(!displayName.toLowerCase(Locale.ROOT).contains(searchTermLowerCase)){
                    iterator.remove();
                }
            }else if(item instanceof SearchResultItem.MessageResult || item instanceof SearchResultItem.LoadingResult){
                return;
            }
        }
    }

    private List<ConversationResponseDTO> getConversations(){
        return conversationList.getItems().stream()
                .filter(item -> item instanceof SearchResultItem.ConversationResult)
                .map(item -> ((SearchResultItem.ConversationResult) item).conversation())
                .toList();
    }
}