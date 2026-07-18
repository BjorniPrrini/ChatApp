package com.chatappbackend.backend.config;

import com.chatappbackend.backend.util.JwtUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    public StompAuthInterceptor(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(accessor.getCommand() == StompCommand.CONNECT){
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                throw new MessagingException("Invalid token");
            }

            String token = authHeader.substring(7);

            boolean valid = jwtUtil.isValid(token);

            if(!valid){
                throw new MessagingException("Invalid token");
            }

            Long userId = jwtUtil.extractUserId(token);

            accessor.setUser(() -> String.valueOf(userId));
        }

        return message;
    }
}