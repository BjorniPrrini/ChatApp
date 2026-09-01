package com.chatappbackend.backend.controller;

import com.chatappbackend.backend.dto.conversation.*;
import com.chatappbackend.backend.entity.User;
import com.chatappbackend.backend.service.conversation.ConversationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    private final ConversationService service;

    public ConversationController(ConversationService service){
        this.service = service;
    }

    @PostMapping("/createConversation")
    public ResponseEntity<ConversationResponseDTO> createConversation(@RequestBody ConversationRequestDTO request){
        ConversationResponseDTO response = service.createConversation(getUser().getId(), request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/createGroupConversation")
    public ResponseEntity<ConversationResponseDTO> createGroupConversation(@RequestBody GroupConversationRequestDTO request){
        ConversationResponseDTO response = service.createGroupConversation(getUser().getId(), request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserConversations")
    public ResponseEntity<List<ConversationResponseDTO>> getUserConversations(){
        return ResponseEntity.ok(service.getUserConversations(getUser().getId()));
    }

    @GetMapping("/{conversationId}")
    public  ResponseEntity<ConversationResponseDTO> getConversationById(@PathVariable Long conversationId){
        return ResponseEntity.ok(service.getConversationById(getUser().getId(), conversationId));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId){
        service.deleteConversation(getUser().getId(), conversationId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/convesation/{conversationId}/participant/{participantToKickId}")
    public ResponseEntity<Void> kickParticipant(@PathVariable Long participantToKickId, @PathVariable Long conversationId){
        service.kickParticipant(getUser().getId(), participantToKickId, conversationId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/leaveGroup/{groupId}")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId){
        service.leaveGroup(getUser().getId(), groupId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversation/{conversationId}/addParticipant/{participantToAddId}")
    public ResponseEntity<ParticipantDTO> addParticipant(@PathVariable Long participantToAddId, @PathVariable Long conversationId){
        return ResponseEntity.ok(service.addParticipant(getUser().getId(), participantToAddId, conversationId));
    }

    @PatchMapping("/conversation/{conversationId}/demoteUser/{demoteUserId}")
    public ResponseEntity<Void> demoteAdmin(@PathVariable Long conversationId, @PathVariable Long demoteUserId){
        service.demoteAdminToUser(getUser().getId(), demoteUserId, conversationId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/conversation/{conversationId}/promoteUser/{promoteUserId}")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Long conversationId, @PathVariable Long promoteUserId){
        service.promoteUserToAdmin(getUser().getId(), promoteUserId, conversationId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> updateGroupInfo(@PathVariable Long conversationId, @RequestBody UpdateGroupRequestDTO request){
        service.updateGroupDetails(request, getUser().getId(), conversationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversation/{conversationId}/participant/self")
    public ResponseEntity<Boolean> isAdmin(@PathVariable Long conversationId){
        return ResponseEntity.ok(service.isAdmin(getUser().getId(), conversationId));
    }

    @GetMapping("/notInConversation/{conversationId}")
    public ResponseEntity<List<ParticipantDTO>> friendsNotInConversation(@PathVariable Long conversationId){
        return ResponseEntity.ok(service.getFriendsNotInGroup(getUser().getId(), conversationId));
    }

    @PatchMapping("/conversation/{conversationId}/allowInvite/{allow}")
    public ResponseEntity<Void> allowParticipantInvite(@PathVariable Long conversationId, @PathVariable boolean allow){
        service.allowParticipantsInvite(conversationId, getUser().getId(), allow);

        return ResponseEntity.ok().build();
    }

    private User getUser(){
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}