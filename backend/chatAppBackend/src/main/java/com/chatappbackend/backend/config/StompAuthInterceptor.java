package com.chatappbackend.backend.config;

import com.chatappbackend.backend.util.JwtUtil;

import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    public StompAuthInterceptor(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(accessor.getCommand() == StompCommand.CONNECT){
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                throw new MessagingException("Invalid token");
            }

            String token = authHeader.substring(7);

            if(!jwtUtil.isValid(token)){
                throw new MessagingException("Invalid token");
            }

            Long userId = jwtUtil.extractUserId(token);

            sessionUsers.put(accessor.getSessionId(), userId);
        }

        Long userId = sessionUsers.get(accessor.getSessionId());

        if(userId != null){
            accessor.setUser(() -> String.valueOf(userId));
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    public Long getUserId(String sessionId){
        return sessionUsers.get(sessionId);
    }

    public void removeSession(String sessionId){
        sessionUsers.remove(sessionId);
    }
}