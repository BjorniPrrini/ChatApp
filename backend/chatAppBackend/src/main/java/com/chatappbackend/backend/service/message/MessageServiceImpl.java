package com.chatappbackend.backend.service.message;

import com.chatappbackend.backend.dto.message.MessageEventDTO;
import com.chatappbackend.backend.dto.message.MessagePageDTO;
import com.chatappbackend.backend.dto.message.MessageRequestDTO;
import com.chatappbackend.backend.dto.message.MessageResponseDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageDelete;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ForbiddenException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.repository.*;
import com.chatappbackend.backend.service.blocked.BlockedUserService;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService{
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageDeleteRepository messageDeleteRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final BlockedUserService blockedUserService;
    private final FriendRequestRepository friendRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(UserRepository userRepository, ConversationRepository conversationRepository, MessageRepository messageRepository, MessageDeleteRepository messageDeleteRepository, ConversationParticipantRepository conversationParticipantRepository, BlockedUserService blockedUserService, FriendRequestRepository friendRequestRepository, SimpMessagingTemplate messagingTemplate){
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageDeleteRepository = messageDeleteRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.blockedUserService = blockedUserService;
        this.friendRequestRepository = friendRequestRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public MessageResponseDTO sendMessage(Long userId, MessageRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User otherUser = conversationParticipantRepository.findOtherParticipant(request.getConversationId(), userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if(blockedUserService.isBlocked(userId, otherUser.getId())){
            throw new BadRequestException("Cannot send message to this user");
        }

        if(!friendRequestRepository.areFriends(userId, otherUser.getId())){
            throw new BadRequestException("You must be friends to message this user");
        }

        Message message = new Message();

        message.setMessage(request.getMessage());
        message.setSender(user);
        message.setConversation(conversation);
        message.setSentAt(LocalDateTime.now());
        message.setStatus("sent");

        if(request.getReplyToId() != null){
            Message replyTo = messageRepository.findById(request.getReplyToId()).orElseThrow(() -> new ResourceNotFoundException("Message not found"));

            if(!replyTo.getConversation().getId().equals(conversation.getId())){
                throw new BadRequestException("Reply message does not belong to this conversation");
            }

            message.setReplyTo(replyTo);
        }

        Message savedMessage = messageRepository.save(message);

        MessageResponseDTO dto = mapToDTO(savedMessage);

        messagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), new MessageEventDTO("NEW", conversation.getId(), dto.getId(), dto, null, null));
        messagingTemplate.convertAndSend("/queue/user." + otherUser.getId(), new MessageEventDTO("NEW", conversation.getId(), dto.getId(), dto, null, null));

        return dto;
    }

    @Override
    public MessagePageDTO getMessages(Long userId, Long conversationId, LocalDateTime before) {
        markConversationAsRead(userId, conversationId);

        List<Message> messages = messageRepository.findMessages(conversationId, before, PageRequest.of(0, 50));

        List<MessageResponseDTO> messagesResponse = messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        Collections.reverse(messagesResponse);

        MessagePageDTO messagesDto = new MessagePageDTO();

        messagesDto.setMessages(messagesResponse);
        messagesDto.setHasMore(messages.size() == 50);

        return messagesDto;
    }

    @Override
    public void deleteMessageForMe(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if(!message.getSender().getId().equals(userId)){
            throw new ForbiddenException("You can only delete your own message");
        }

        if(message.getSentAt().isBefore(LocalDateTime.now().minusMinutes(1))){
            throw new BadRequestException("You can't delete a message after 1 minutes");
        }

        Long conversationId = message.getConversation().getId();

        messageRepository.delete(message);

        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, new MessageEventDTO("DELETE", conversationId, messageId, null, null, null));
    }

    @Override
    public MessageResponseDTO editMessage(Long userId, Long messageId, String newMessage) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if(!Objects.equals(message.getSender().getId(), userId)){
            throw new ForbiddenException("You are not this message sender");
        }

        if(message.getSentAt().isBefore(LocalDateTime.now().minusMinutes(15))){
            throw new BadRequestException("You can't edit a message after 15 minutes");
        }

        message.setMessage(newMessage);
        message.setEditedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        MessageResponseDTO dto = mapToDTO(savedMessage);

        messagingTemplate.convertAndSend("/topic/conversation." + savedMessage.getConversation().getId(), new MessageEventDTO("EDIT", savedMessage.getConversation().getId(), savedMessage.getId(), dto, null, null));

        return dto;
    }

    @Override
    public void markConversationAsRead(Long userId, Long conversationId) {
        List<Long> unreadMessagesList = messageRepository.findUnreadMessageIds(conversationId, userId);

        messageRepository.markMessagesAsRead(conversationId, userId);

        if(!unreadMessagesList.isEmpty()){
            MessageEventDTO statusEvent = new MessageEventDTO();

            statusEvent.setType("STATUS");
            statusEvent.setConversationId(conversationId);
            statusEvent.setMessageIds(unreadMessagesList);
            statusEvent.setStatus("read");

            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, statusEvent);

            conversationParticipantRepository.findOtherParticipant(conversationId, userId)
                    .ifPresent(sender -> messagingTemplate.convertAndSend("/queue/user." + sender.getId(), statusEvent));
        }
    }

    @Override
    public void markMessageAsDelivered(Long messageId){
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        messageRepository.markAsDelivered(messageId);

        MessageEventDTO messageEventDTO = new MessageEventDTO();

        messageEventDTO.setConversationId(message.getConversation().getId());
        messageEventDTO.setType("STATUS");
        messageEventDTO.setMessageIds(List.of(messageId));
        messageEventDTO.setStatus("delivered");

        messagingTemplate.convertAndSend("/topic/conversation." + message.getConversation().getId(), messageEventDTO);
        messagingTemplate.convertAndSend("/queue/user." + message.getSender().getId(), messageEventDTO);
    }

    @Override
    @Transactional
    public void markAllUndeliveredAsDelivered(Long userId){
        List<UndeliveredMessageProjection> undelivered = messageRepository.findUndeliveredMessages(userId);

        if(undelivered.isEmpty()){
            return;
        }

        List<Long> messageIds = undelivered.stream()
                .map(UndeliveredMessageProjection::getMessageId)
                .toList();

        messageRepository.markMessagesAsDelivered(messageIds);

        Map<Long, List<UndeliveredMessageProjection>> byConversation = undelivered.stream()
                .collect(Collectors.groupingBy(UndeliveredMessageProjection::getConversationId));

        for(Map.Entry<Long, List<UndeliveredMessageProjection>> entry : byConversation.entrySet()){
            Long conversationId = entry.getKey();
            List<UndeliveredMessageProjection> messagesInConversation = entry.getValue();

            List<Long> idsInConversation = messagesInConversation.stream()
                    .map(UndeliveredMessageProjection::getMessageId)
                    .toList();

            MessageEventDTO event = new MessageEventDTO();

            event.setConversationId(conversationId);
            event.setType("STATUS");
            event.setMessageIds(idsInConversation);
            event.setStatus("delivered");

            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, event);

            Set<Long> senderIds = messagesInConversation.stream()
                    .map(UndeliveredMessageProjection::getSenderId)
                    .collect(Collectors.toSet());

            for(Long senderId : senderIds){
                messagingTemplate.convertAndSend("/queue/user." + senderId, event);
            }
        }
    }

    private MessageResponseDTO mapToDTO(Message message){
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