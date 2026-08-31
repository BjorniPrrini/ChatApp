package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.*;
import com.chatappbackend.backend.entity.*;
import com.chatappbackend.backend.exception.BadRequestException;
import com.chatappbackend.backend.exception.ForbiddenException;
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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
        if(conversationRepository.existsGroupByNameForUser(userId,request.getGroupName())){
            throw new BadRequestException("You already have a group with this name");
        }

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

    @Override
    @Transactional
    public void kickParticipant(Long userId, Long removeUserId, Long conversationId) {
        ConversationParticipant admin = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if(!admin.isAdmin()){
            throw new ForbiddenException("You aren't an admin in this conversation");
        }

        removeParticipant(removeUserId, conversationId);
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId, Long conversationId) {
        removeParticipant(userId, conversationId);
    }

    @Override
    @Transactional
    public void removeParticipant(Long removeUserId, Long conversationId) {
        ConversationParticipant participantToRemove = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, removeUserId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        participantToRemove.setLeftAt(LocalDateTime.now());

        conversationParticipantRepository.save(participantToRemove);

        if(!participantToRemove.isAdmin()){
            return;
        }

        List<ConversationParticipant> participantsActiveList = conversationParticipantRepository.findActiveParticipants(conversationId);

        if(participantsActiveList.isEmpty()){
            return;
        }

        List<ConversationParticipant> adminList = participantsActiveList.stream()
                .filter(ConversationParticipant::isAdmin)
                .toList();

        if(!adminList.isEmpty()){
            return;
        }

        ConversationParticipant chosenAdmin = participantsActiveList.get(ThreadLocalRandom.current().nextInt(participantsActiveList.size()));

        chosenAdmin.setAdmin(true);

        conversationParticipantRepository.save(chosenAdmin);
    }

    @Override
    @Transactional
    public ParticipantDTO addParticipant(Long userId, Long addedUserId, Long conversationId) {
        ConversationParticipant admin = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User addedUser = userRepository.findById(addedUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!admin.isAdmin() && !conversation.isAllowParticipantsInvite()){
            throw new ForbiddenException("You are not an admin and you don't have permission to add users");
        }

        Optional<ConversationParticipant> existingParticipant = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, addedUserId);

        if(existingParticipant.isEmpty()){
            ConversationParticipant cp = new ConversationParticipant();

            cp.setAdmin(false);
            cp.setJoinedAt(LocalDateTime.now());
            cp.setConversation(conversation);
            cp.setUser(addedUser);

            conversationParticipantRepository.save(cp);

            return conversationMapper.toParticipantDTO(addedUser);
        }

        ConversationParticipant conversationParticipant = existingParticipant.get();

        if(conversationParticipant.getLeftAt() == null){
            throw new BadRequestException("User is already a participant");
        }

        conversationParticipant.setJoinedAt(LocalDateTime.now());
        conversationParticipant.setLeftAt(null);

        conversationParticipantRepository.save(conversationParticipant);

        return conversationMapper.toParticipantDTO(addedUser);
    }

    @Override
    public void promoteUserToAdmin(Long userId, Long promoteUserId, Long conversationId){
        ConversationParticipant admin = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        ConversationParticipant userToPromote = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, promoteUserId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if(!admin.isAdmin()){
            throw new ForbiddenException("You are not an admin");
        }

        if(userToPromote.isAdmin()){
            throw new BadRequestException("Participant is already an admin");
        }

        userToPromote.setAdmin(true);

        conversationParticipantRepository.save(userToPromote);
    }

    @Override
    public void demoteAdminToUser(Long userId, Long demoteUserId, Long conversationId){
        ConversationParticipant admin = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        ConversationParticipant adminToDemote = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, demoteUserId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if(!admin.isAdmin()){
            throw new ForbiddenException("You are not an admin");
        }

        if(!adminToDemote.isAdmin()){
            throw new BadRequestException("Participant is not an admin");
        }

        List<ConversationParticipant> participants = conversationParticipantRepository.findActiveParticipants(conversationId);

        List<ConversationParticipant> admins = participants.stream()
                .filter(ConversationParticipant::isAdmin)
                .toList();

        if(admins.size() == 1){
            throw new BadRequestException("There is only 1 admin left");
        }

        adminToDemote.setAdmin(false);

        conversationParticipantRepository.save(adminToDemote);
    }

    @Override
    public void updateGroupDetails(UpdateGroupRequestDTO request, Long userId, Long conversationId) {
        ConversationParticipant user = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if(user.getLeftAt() != null){
            throw new ForbiddenException("You are not part of this conversation anymore");
        }

        if(request.getGroupPicture() != null){
            conversation.setGroupPicture(request.getGroupPicture());
        }

        if(request.getGroupName() != null){
            conversation.setName(request.getGroupName());
        }

        conversationRepository.save(conversation);
    }

    @Override
    public Boolean isAdmin(Long id, Long conversationId) {
        ConversationParticipant user = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, id).orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        return user.isAdmin();
    }

    @Override
    public List<ParticipantDTO> getFriendsNotInGroup(Long userId, Long conversationId) {
        List<User> friendsNotInConversation = friendRequestRepository.findFriendsNotInConversation(userId, conversationId);

        return friendsNotInConversation.stream()
                .map(conversationMapper::toParticipantDTO)
                .toList();
    }

    private Optional<Message> getLastMessage(Long conversationId){
        return messageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId);
    }
}