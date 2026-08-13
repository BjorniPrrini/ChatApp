package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.dto.conversation.GroupConversationRequestDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

            return conversationMapper.toConversationResponseDTO(existing.get(), List.of(receiver), getLastMessage(existing.get().getId()));
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

        return conversationMapper.toConversationResponseDTO(savedConversation, List.of(receiver), Optional.empty());
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> {
                    List<User> otherUserList = conversationParticipantRepository.findOtherParticipants(conversation.getId(), userId);

                    if(otherUserList.isEmpty()){
                        throw new ResourceNotFoundException("Participant not found");
                    }

                    return conversationMapper.toConversationResponseDTO(conversation, otherUserList, getLastMessage(conversation.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponseDTO getConversationById(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        List<User> otherUserList = conversationParticipantRepository.findOtherParticipants(conversation.getId(), userId);

        if(otherUserList.isEmpty()){
            throw new ResourceNotFoundException("Participant not found");
        }

        return conversationMapper.toConversationResponseDTO(conversation, otherUserList, getLastMessage(conversationId));
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

    @Transactional
    @Override
    public ConversationResponseDTO createGroupConversation(Long userId, GroupConversationRequestDTO request) {
        if(request.getParticipants() == null || request.getParticipants().size() < 2){
            throw new BadRequestException("A group conversation must have at least 3 members");
        }

        List<Long> participantsList = new ArrayList<>(request.getParticipants());

        if(participantsList.contains(userId)){
            throw new BadRequestException("You are already included as the group creator");
        }

        if(participantsList.stream().distinct().count() != participantsList.size()){
            throw new BadRequestException("Duplicate participants are not allowed");
        }

        participantsList.add(userId);

        for(int i = 0; i < participantsList.size(); i++){
            for(int j = i + 1; j < participantsList.size(); j++){
                if(blockedUserService.isBlocked(participantsList.get(i), participantsList.get(j))){
                    throw new BadRequestException("There is a blocking conflict between selected group members");
                }
            }
        }

        for(Long participant : request.getParticipants()){
            if(!friendRequestRepository.areFriends(userId, participant)){
                throw new BadRequestException("You are not friends with this user");
            }
        }

        Conversation conversation = new Conversation();

        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setIsGroup(true);
        conversation.setName(request.getGroupName());
        conversation.setGroupPicture(request.getGroupPicture());

        Conversation savedConversation = conversationRepository.save(conversation);

        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ConversationParticipant creatorParticipant = new ConversationParticipant();

        creatorParticipant.setUser(creator);
        creatorParticipant.setConversation(savedConversation);
        creatorParticipant.setJoinedAt(LocalDateTime.now());
        creatorParticipant.setAdmin(true);

        List<ConversationParticipant> participants = new ArrayList<>();

        participants.add(creatorParticipant);

        List<User> participantList = new ArrayList<>();

        for(Long id : request.getParticipants()){
            User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            ConversationParticipant participant = new ConversationParticipant();

            participant.setUser(user);
            participant.setConversation(savedConversation);
            participant.setJoinedAt(LocalDateTime.now());
            participant.setAdmin(false);

            participants.add(participant);
            participantList.add(user);
        }

        conversationParticipantRepository.saveAll(participants);

        return conversationMapper.toConversationResponseDTO(savedConversation, participantList, getLastMessage(savedConversation.getId()));
    }

    private Optional<Message> getLastMessage(Long conversationId){
        return messageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId);
    }
}