package com.chatappfrontend.frontend.manager;

import com.chatappfrontend.frontend.model.MessageEventDTO;
import com.chatappfrontend.frontend.model.UserStatusEventDTO;
import com.chatappfrontend.frontend.service.WebSocketService;

import java.util.function.Consumer;

public class WebSocketConnectionManager {
    private final WebSocketService webSocketService;

    public WebSocketConnectionManager(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public void connect(Long userId, Consumer<MessageEventDTO> onUserEvent, Consumer<UserStatusEventDTO> onStatusEvent) throws Exception {
        webSocketService.connect();
        webSocketService.subscribeToUser(userId, onUserEvent);
        webSocketService.subscribeToStatus(userId, onStatusEvent);
        webSocketService.sendMarkAllDeliveredRequest();
        webSocketService.sendOnlineStatusRequest();
    }

    public void subscribeToConversation(Long conversationId, Consumer<MessageEventDTO> onConversationEvent) {
        webSocketService.unsubscribe();
        webSocketService.subscribe(conversationId, onConversationEvent);
    }
}