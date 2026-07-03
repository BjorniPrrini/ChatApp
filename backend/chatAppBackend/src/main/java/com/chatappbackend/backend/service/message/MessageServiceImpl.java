package com.chatappbackend.backend.service.message;

import com.chatappbackend.backend.dto.message.MessagePageDTO;
import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageDelete;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService{
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageDeleteRepository messageDeleteRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final BlockedUserRepository blockedUserRepository;

    public MessageServiceImpl(UserRepository userRepository, ConversationRepository conversationRepository, MessageRepository messageRepository, MessageDeleteRepository messageDeleteRepository, ConversationParticipantRepository conversationParticipantRepository, BlockedUserRepository blockedUserRepository){
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageDeleteRepository = messageDeleteRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.blockedUserRepository = blockedUserRepository;
    }

    @Override
    public MessageResponseDTO sendMessage(Long userId, MessageRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();

        message.setMessage(request.getMessage());
        message.setSender(user);
        message.setConversation(conversation);
        message.setSentAt(LocalDateTime.now());
        message.setStatus("sent");

        if(request.getReplyToId() != null){
            Message replyTo = messageRepository.findById(request.getReplyToId()).orElseThrow(() -> new RuntimeException("Message not found"));

            message.setReplyTo(replyTo);
        }

        Message savedMessage = messageRepository.save(message);

        return mapToDto(savedMessage);
    }

    @Override
    public MessagePageDTO getMessages(Long userId, Long conversationId, LocalDateTime before) {
        List<Message> messages = messageRepository.findMessages(conversationId, before, PageRequest.of(0, 50));

        List<MessageResponseDTO> messagesResponse = messages.stream()
                .map(message -> mapToDto(message))
                .collect(Collectors.toList());

        MessagePageDTO messagesDto = new MessagePageDTO();

        messagesDto.setMessages(messagesResponse);
        messagesDto.setHasMore(messages.size() == 50);

        return messagesDto;
    }

    @Override
    public void deleteMessageForMe(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        MessageDelete messageDelete = new MessageDelete();

        messageDelete.setMessage(message);
        messageDelete.setUser(user);
        messageDelete.setDeletedAt(LocalDateTime.now());

        messageDeleteRepository.save(messageDelete);

        long deleteCount = messageDeleteRepository.countByMessage(message);

        long participantCount = conversationParticipantRepository.countByConversationId(message.getConversation().getId());

        if(deleteCount >= participantCount){
            messageRepository.delete(message);
        }
    }

    @Override
    public void deleteMessageForEveryone(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        if(!message.getSender().getId().equals(userId)){
            throw new RuntimeException("You can only delete your own message");
        }

        if(message.getSentAt().isBefore(LocalDateTime.now().minusMinutes(1))){
            throw new RuntimeException("You can't delete a message after 1 minutes");
        }

        messageRepository.delete(message);
    }

    @Override
    public MessageResponseDTO editMessage(Long userId, Long messageId, String newMessage) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        if(!Objects.equals(message.getSender().getId(), userId)){
            throw new RuntimeException("You are not this message sender");
        }

        if(message.getSentAt().isBefore(LocalDateTime.now().minusMinutes(15))){
            throw new RuntimeException("You can't edit a message after 15 minutes");
        }

        message.setMessage(newMessage);
        message.setEditedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        return mapToDto(savedMessage);
    }

    private MessageResponseDTO mapToDto(Message message){
        MessageResponseDTO response = new MessageResponseDTO();

        response.setMessage(message.getMessage());
        response.setId(message.getId());
        response.setSentAt(message.getSentAt());
        response.setStatus(message.getStatus());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getName());
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
}