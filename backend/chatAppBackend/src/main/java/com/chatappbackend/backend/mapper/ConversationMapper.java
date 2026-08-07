package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.User;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConversationMapper {
    public ConversationResponseDTO toConversationResponseDTO(Conversation conversation, User receiver, Optional<Message> lastMessage){
        ConversationResponseDTO response = new ConversationResponseDTO();

        response.setConversationId(conversation.getId());
        response.setOtherUserId(receiver.getId());
        response.setName(receiver.getName());
        response.setSurname(receiver.getSurname());
        response.setNickname(receiver.getNickname());
        response.setProfilePicture(receiver.getProfilePicture());
        response.setOnline(receiver.isOnline());
        response.setGroup(false);

        if(lastMessage.isPresent()){
            response.setLastMessage(lastMessage.get().getMessage());
            response.setLastMessageAt(lastMessage.get().getSentAt());
        }else{
            response.setLastMessage(null);
            response.setLastMessageAt(null);
        }

        return response;
    }
}