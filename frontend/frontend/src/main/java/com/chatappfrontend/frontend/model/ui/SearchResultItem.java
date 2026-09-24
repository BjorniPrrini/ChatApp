package com.chatappfrontend.frontend.model.ui;

import com.chatappfrontend.frontend.model.ConversationResponseDTO;
import com.chatappfrontend.frontend.model.MessageSearchResultResponseDTO;

public sealed interface SearchResultItem permits SearchResultItem.ConversationResult, SearchResultItem.MessageResult, SearchResultItem.LoadingResult {
    record ConversationResult(ConversationResponseDTO conversation) implements SearchResultItem {}
    record MessageResult(MessageSearchResultResponseDTO message) implements SearchResultItem {}
    record LoadingResult() implements SearchResultItem {}
}