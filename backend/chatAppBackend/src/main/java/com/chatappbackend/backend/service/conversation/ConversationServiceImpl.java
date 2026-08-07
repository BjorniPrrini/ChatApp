package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.ConversationParticipant;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.mapper.ConversationMapper;
import com.chatappbackend.backend.repository.*;
import com.chatappbackend.backend.service.blocked.BlockedUserService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService{
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final UserRepository userRepository;
    private final BlockedUserService blockedUserService;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;

    public ConversationServiceImpl(ConversationRepository conversationRepository, ConversationParticipantRepository conversationParticipantRepository, UserRepository userRepository, BlockedUserService blockedUserService, FriendRequestRepository friendRequestRepository, MessageRepository messageRepository, ConversationMapper conversationMapper){
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.userRepository = userRepository;
        this.blockedUserService = blockedUserService;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;
    }

    @Override
    public ConversationResponseDTO createConversation(Long userId, ConversationRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if(blockedUserService.isBlocked(userId, receiver.getId())){
            throw new BadRequestException("This user is blocked");
        }

        if(!friendRequestRepository.areFriends(userId, receiver.getId())){
            throw new BadRequestException("You must be friends to message this user");
        }

        Optional<Conversation> existing = conversationRepository.findDMBetweenUsers(userId, request.getReceiverId());

        if(existing.isPresent()){
            conversationParticipantRepository.restoreForUser(existing.get().getId(), userId);

            return conversationMapper.toConversationResponseDTO(existing.get(), receiver, getLastMessage(existing.get().getId()));
        }

        Conversation conversation = new Conversation();

        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setIsGroup(false);

        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant participant1 = new ConversationParticipant();

        participant1.setConversation(savedConversation);
        participant1.setJoinedAt(LocalDateTime.now());
        participant1.setUser(user);

        ConversationParticipant participant2 = new ConversationParticipant();

        participant2.setConversation(savedConversation);
        participant2.setJoinedAt(LocalDateTime.now());
        participant2.setUser(receiver);

        conversationParticipantRepository.save(participant1);
        conversationParticipantRepository.save(participant2);

        return conversationMapper.toConversationResponseDTO(savedConversation, receiver, Optional.empty());
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> {
                    User otherUser = conversationParticipantRepository.findOtherParticipant(conversation.getId(), userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

                    return conversationMapper.toConversationResponseDTO(conversation, otherUser, getLastMessage(conversation.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponseDTO getConversationById(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User otherParticipant = conversationParticipantRepository.findOtherParticipant(conversation.getId(), userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        return conversationMapper.toConversationResponseDTO(conversation, otherParticipant, getLastMessage(conversationId));
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        LocalDateTime now = LocalDateTime.now();

        conversationParticipantRepository.markUserDeleteCutoff(conversationId, userId, now);

        conversationParticipantRepository.softDeleteForUser(conversationId, userId, now);

        long deletedParticipants = conversationParticipantRepository.countByConversationIdAndDeletedAtIsNotNull(conversationId);

        long participantCount = conversationParticipantRepository.countByConversationId(conversationId);

        if(deletedParticipants >= participantCount){
            conversationRepository.deleteById(conversationId);
        }
    }

    private Optional<Message> getLastMessage(Long conversationId){
        return messageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId);
    }
}