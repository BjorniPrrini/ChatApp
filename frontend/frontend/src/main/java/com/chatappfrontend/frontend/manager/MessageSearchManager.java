package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.MessageSearchResultResponseDTO;
import com.chatappfrontend.frontend.model.ui.SearchResultItem;
import com.chatappfrontend.frontend.service.EmbeddingService;
import com.chatappfrontend.frontend.util.AppExecutor;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.function.Consumer;

public class MessageSearchManager {
    private final ListView<SearchResultItem> conversationList;
    private final Consumer<String> onError;
    private final EmbeddingService embeddingService = new EmbeddingService();

    private boolean searching = false;

    public MessageSearchManager(ListView<SearchResultItem> conversationList, Consumer<String> onError) {
        this.conversationList = conversationList;
        this.onError = onError;
    }

    public void search(String query){
        if(searching){
            return;
        }

        searching = true;

        conversationList.getItems().add(new SearchResultItem.LoadingResult());

        Task<List<MessageSearchResultResponseDTO>> searchTask = new Task<>() {
            @Override
            protected List<MessageSearchResultResponseDTO> call() throws Exception {
                return embeddingService.searchMessages(query);
            }
        };

        searchTask.setOnSucceeded(_ -> {
            removeLoadingResult();

            for(MessageSearchResultResponseDTO message : searchTask.getValue()){
                conversationList.getItems().add(new SearchResultItem.MessageResult(message));
            }

            searching = false;
        });

        searchTask.setOnFailed(_ -> {
            removeLoadingResult();

            onError.accept("Search failed");

            searching = false;
        });

        AppExecutor.run(searchTask);
    }

    public void clearSearchResults(){
        conversationList.getItems().removeIf(item -> item instanceof SearchResultItem.LoadingResult || item instanceof SearchResultItem.MessageResult);
    }

    private void removeLoadingResult(){
        conversationList.getItems().removeIf(item -> item instanceof SearchResultItem.LoadingResult);
    }
}