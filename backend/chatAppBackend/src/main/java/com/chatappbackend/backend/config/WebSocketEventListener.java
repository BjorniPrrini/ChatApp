package com.chatappbackend.backend.config;

import com.chatappbackend.backend.service.user.UserService;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    private final StompAuthInterceptor stompAuthInterceptor;
    private final UserService userService;

    public WebSocketEventListener(StompAuthInterceptor stompAuthInterceptor, UserService userService) {
        this.stompAuthInterceptor = stompAuthInterceptor;
        this.userService = userService;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        Long userId = stompAuthInterceptor.getUserId(sessionId);

        if(userId != null){
            userService.setOfflineUser(userId);
        }

        stompAuthInterceptor.removeSession(sessionId);
    }
}