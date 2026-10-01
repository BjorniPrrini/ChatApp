package com.chatappbackend.backend.service.summary;

import com.chatappbackend.backend.dto.summary.SummaryResponseDTO;
import com.chatappbackend.backend.entity.*;
import com.chatappbackend.backend.exception.ForbiddenException;
import com.chatappbackend.backend.exception.ResourceNotFoundException;
import com.chatappbackend.backend.exception.SummaryGenerationException;
import com.chatappbackend.backend.exception.SummaryInProgressException;
import com.chatappbackend.backend.mapper.SummaryMapper;
import com.chatappbackend.backend.repository.*;
import com.chatappbackend.backend.service.lock.LockService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SummaryServiceImpl implements SummaryService{
    private final ConversationSummaryRepository conversationSummaryRepository;
    private final MessageRepository messageRepository;
    private final SummaryMapper summaryMapper;
    private final UserSummaryQuotaRepository userSummaryQuotaRepository;
    private final LockService lockService;
    private final ConversationRepository conversationRepository;
    private final GeminiSummaryClient geminiSummaryClient;
    private final ConversationParticipantRepository conversationParticipantRepository;

    public SummaryServiceImpl(ConversationSummaryRepository conversationSummaryRepository, MessageRepository messageRepository, SummaryMapper summaryMapper, UserSummaryQuotaRepository userSummaryQuotaRepository, LockService lockService, ConversationRepository conversationRepository, GeminiSummaryClient geminiSummaryClient, ConversationParticipantRepository conversationParticipantRepository) {
        this.conversationSummaryRepository = conversationSummaryRepository;
        this.messageRepository = messageRepository;
        this.summaryMapper = summaryMapper;
        this.userSummaryQuotaRepository = userSummaryQuotaRepository;
        this.lockService = lockService;
        this.conversationRepository = conversationRepository;
        this.geminiSummaryClient = geminiSummaryClient;
        this.conversationParticipantRepository = conversationParticipantRepository;
    }

    @Override
    public SummaryResponseDTO getSummary(Long userId, Long conversationId) {
        Optional<ConversationSummary> summary = conversationSummaryRepository.findById(conversationId);
        UserSummaryQuota userSummaryQuota = userSummaryQuotaRepository.findById(userId).orElseGet(() -> registerUser(userId));

        conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ForbiddenException("User does not belong in this conversation"));

        if(userSummaryQuota.getLastResetDate().isBefore(LocalDate.now())){
            userSummaryQuota.setRemainingQuota(5);
            userSummaryQuota.setLastResetDate(LocalDate.now());

            userSummaryQuotaRepository.save(userSummaryQuota);
        }

        if(userSummaryQuota.getRemainingQuota() == 0){
            if(summary.isPresent()){
                return cachedSummary(conversationId, summary.get().getSummary(), userSummaryQuota);
            }

            throw new ForbiddenException("No quota left for today");
        }

        if(summary.isEmpty()){
            return summarize(conversationId, userSummaryQuota);
        }

        if(summary.get().getLastMessageId() == null){
            return summarize(conversationId, userSummaryQuota);
        }

        if(messageRepository.countMessagesAfter(conversationId, summary.get().getLastMessageId()) < 10){
            return cachedSummary(conversationId, summary.get().getSummary(), userSummaryQuota);
        }

        return summarize(conversationId, userSummaryQuota);
    }

    private SummaryResponseDTO summarize(Long conversationId, UserSummaryQuota userSummaryQuota){
        String token = UUID.randomUUID().toString();

        boolean isAcquired = lockService.tryAcquire(conversationId, token, Duration.ofMinutes(2));

        if(!isAcquired){
            throw new SummaryInProgressException("Summary is in progress");
        }

        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        int messageNumber;

        if(conversation.getIsGroup()){
            messageNumber = 100;
        }else{
            messageNumber = 50;
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByIdDesc(conversationId, PageRequest.of(0, messageNumber));

        if(messages.isEmpty()){
            throw new ResourceNotFoundException("No messages in conversation");
        }

        Collections.reverse(messages);

        Message lastMessage = messages.getLast();

        StringBuilder stringBuilder = new StringBuilder();

        for(Message m : messages){
            stringBuilder.append(m.getSender().getName()).append(" ").append(m.getSender().getSurname()).append(" ").append(m.getMessage()).append("\n");
        }

        try {
            String response = geminiSummaryClient.generateSummary(stringBuilder.toString());

            ConversationSummary conversationSummary = new ConversationSummary();

            conversationSummary.setLastMessageId(lastMessage.getId());
            conversationSummary.setSummary(response);
            conversationSummary.setConversationId(conversationId);

            userSummaryQuota.setRemainingQuota(userSummaryQuota.getRemainingQuota() - 1);

            conversationSummaryRepository.save(conversationSummary);
            userSummaryQuotaRepository.save(userSummaryQuota);

            return summaryMapper.toSummaryResponseDTO(conversationId, response, userSummaryQuota.getRemainingQuota());
        } catch (Exception _) {
            throw new SummaryGenerationException("Couldn't create summary");
        } finally {
            lockService.release(conversationId, token);
        }
    }

    private SummaryResponseDTO cachedSummary(Long conversationId, String summary, UserSummaryQuota userSummaryQuota){
        return summaryMapper.toSummaryResponseDTO(conversationId, summary, userSummaryQuota.getRemainingQuota());
    }

    private UserSummaryQuota registerUser(Long userId){
        UserSummaryQuota userSummaryQuota = new UserSummaryQuota();

        userSummaryQuota.setUserId(userId);
        userSummaryQuota.setRemainingQuota(5);
        userSummaryQuota.setLastResetDate(LocalDate.now());

        return userSummaryQuotaRepository.save(userSummaryQuota);
    }
}