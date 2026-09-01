package com.chatappbackend.backend.mapper;

import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.dto.conversation.ParticipantDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.User;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConversationMapper {
    public ConversationResponseDTO toConversationResponseDTO(Conversation conversation, List<User> otherUsers, Optional<Message> lastMessage){
        ConversationResponseDTO response = new ConversationResponseDTO();

        response.setConversationId(conversation.getId());
        response.setGroupName(conversation.getName());
        response.setGroupPicture(conversation.getGroupPicture());
        response.setGroup(conversation.getIsGroup());
        response.setParticipants(otherUsers.stream().map(this::toParticipantDTO).toList());
        response.setAllowParticipantsInvite(conversation.isAllowParticipantsInvite());

        if(lastMessage.isPresent()){
            response.setLastMessage(lastMessage.get().getMessage());
            response.setLastMessageAt(lastMessage.get().getSentAt());
        }else{
            response.setLastMessage(null);
            response.setLastMessageAt(null);
        }

        return response;
    }

    public ParticipantDTO toParticipantDTO(User user){
        ParticipantDTO participant = new ParticipantDTO();

        participant.setUserId(user.getId());
        participant.setName(user.getName());
        participant.setSurname(user.getSurname());
        participant.setNickname(user.getNickname());
        participant.setProfilePicture(user.getProfilePicture());
        participant.setOnline(user.isOnline());

        return participant;
    }
}