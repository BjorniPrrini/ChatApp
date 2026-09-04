package com.chatappbackend.backend.service.conversation;

import com.chatappbackend.backend.dto.conversation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConversationService {
    ConversationResponseDTO createConversation(Long userId, ConversationRequestDTO request);
    List<ConversationResponseDTO> getUserConversations(Long userId);
    ConversationResponseDTO getConversationById(Long userId, Long conversationId);
    void deleteConversation(Long userId, Long conversationId);
    ConversationResponseDTO createGroupConversation(Long userId, GroupConversationRequestDTO request);
    void kickParticipant(Long userId, Long removeUserId, Long conversationId);
    void leaveGroup(Long userId, Long conversationId);
    void removeParticipant(Long removeUserId, Long conversationId);
    ParticipantDTO addParticipant(Long userId, Long addedUserId, Long conversationId);
    void demoteAdminToUser(Long userId, Long demoteUserId, Long conversationId);
    void promoteUserToAdmin(Long userId, Long promoteUserId, Long conversationId);
    void updateGroupDetails(Long conversationId, Long userId, String groupName, MultipartFile groupPicture);
    Boolean isAdmin(Long id, Long conversationId);
    List<ParticipantDTO> getFriendsNotInGroup(Long userId, Long conversationId);
    void allowParticipantsInvite(Long conversationId, Long userId, boolean allow);
}