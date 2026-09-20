package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.dto.message.MessageSearchResultDTO;
import com.chatappbackend.backend.entity.Message;

import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageResponseDTO toMessageResponseDTO(Message message){
        MessageResponseDTO response = new MessageResponseDTO();

        response.setMessage(message.getMessage());
        response.setId(message.getId());
        response.setSentAt(message.getSentAt());
        response.setStatus(message.getStatus());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getName());
        response.setSenderSurname(message.getSender().getSurname());
        response.setSenderNickname(message.getSender().getNickname());
        response.setSenderProfilePicture(message.getSender().getProfilePicture());
        response.setEditedAt(message.getEditedAt());
        response.setEdited(message.getEditedAt() != null);

        if(message.getReplyTo() != null){
            response.setReplyToId(message.getReplyTo().getId());
            response.setReplyToMessage(message.getReplyTo().getMessage());
        }

        return response;
    }

    public MessageSearchResultDTO toMessageSearchResultDTO(Message message){
        MessageSearchResultDTO response = new MessageSearchResultDTO();

        response.setMessageId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderName(message.getSender().getName());
        response.setSenderProfilePicture(message.getSender().getProfilePicture());
        response.setMessage(message.getMessage());
        response.setSentAt(message.getSentAt());

        return response;
    }
}