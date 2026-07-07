package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.ConversationRequestDTO;
import com.chatappbackend.backend.dto.conversation.ConversationResponseDTO;
import com.chatappbackend.backend.entity.Conversation;
import com.chatappbackend.backend.entity.ConversationParticipant;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.repository.ConversationParticipantRepository;
import com.chatappbackend.backend.repository.ConversationRepository;
import com.chatappbackend.backend.repository.UserRepository;
import com.chatappbackend.backend.service.blocked.BlockedUserServiceImpl;
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
    private final BlockedUserServiceImpl blockedUserService;

    public ConversationServiceImpl(ConversationRepository conversationRepository, ConversationParticipantRepository conversationParticipantRepository, UserRepository userRepository, BlockedUserServiceImpl blockedUserService){
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.userRepository = userRepository;
        this.blockedUserService = blockedUserService;
    }

    @Override
    public ConversationResponseDTO createConversation(Long userId, ConversationRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new RuntimeException("Receiver not found"));

        if(blockedUserService.isBlocked(userId, receiver.getId())){
            throw new RuntimeException("This user is blocked");
        }

        Optional<Conversation> existing = conversationRepository.findDMBetweenUsers(userId, request.getReceiverId());

        if(existing.isPresent()){
            return mapToDTO(existing.get(), receiver);
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

        return mapToDTO(savedConversation, receiver);
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> {
                    User otherUser = conversationParticipantRepository.findOtherParticipant(conversation.getId(), userId).orElseThrow(() -> new RuntimeException("Participant not found"));

                    return mapToDTO(conversation, otherUser);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponseDTO getConversationById(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        User otherParticipant = conversationParticipantRepository.findOtherParticipant(conversation.getId(), userId).orElseThrow(() -> new RuntimeException("Participant not found"));

        return mapToDTO(conversation, otherParticipant);
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversationParticipantRepository.softDeleteForUser(conversationId, userId, LocalDateTime.now());

        long deletedParticipants = conversationParticipantRepository.countByConversationIdAndDeletedAtIsNotNull(conversationId);

        long participantCount = conversationParticipantRepository.countByConversationId(conversationId);

        if(deletedParticipants >= participantCount){
            conversationRepository.deleteById(conversationId);
        }
    }

    private ConversationResponseDTO mapToDTO(Conversation conversation, User receiver){
        ConversationResponseDTO response = new ConversationResponseDTO();

        response.setConversationId(conversation.getId());
        response.setOtherUserId(receiver.getId());
        response.setName(receiver.getName());
        response.setSurname(receiver.getSurname());
        response.setNickname(receiver.getNickname());
        response.setProfilePicture(receiver.getProfilePicture());
        response.setOnline(receiver.isOnline());
        response.setGroup(false);
        response.setLastMessage(null);
        response.setLastMessageAt(null);

        return response;
    }
}