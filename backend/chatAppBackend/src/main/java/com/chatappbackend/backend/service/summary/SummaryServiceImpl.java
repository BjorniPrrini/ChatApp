package com.chatappbackend.backend.service.summary;

import com.chatappbackend.backend.dto.summary.SummaryResponseDTO;
import com.chatappbackend.backend.entity.*;
import com.chatappbackend.backend.exception.*;
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
        conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId).orElseThrow(() -> new ForbiddenException("User does not belong in this conversation"));

        Optional<ConversationSummary> summary = conversationSummaryRepository.findById(conversationId);
        UserSummaryQuota userSummaryQuota = userSummaryQuotaRepository.findById(userId).orElseGet(() -> registerUser(userId));

        if(userSummaryQuota.getLastResetDate().isBefore(LocalDate.now())){
            userSummaryQuotaRepository.resetQuotaLimit(userId, LocalDate.now());
        }

        int remainingQuota = userSummaryQuotaRepository.findRemainingQuota(userId);

        if(remainingQuota == 0){
            if(summary.isPresent()){
                return cachedSummary(conversationId, summary.get().getSummary(), remainingQuota);
            }

            throw new BadRequestException("No quota left for today");
        }

        if(summary.isEmpty()){
            return summarize(conversationId, userId);
        }

        if(summary.get().getLastMessageId() == null){
            return summarize(conversationId, userId);
        }

        if(messageRepository.countMessagesAfter(conversationId, summary.get().getLastMessageId()) < 10){
            return cachedSummary(conversationId, summary.get().getSummary(), remainingQuota);
        }

        return summarize(conversationId, userId);
    }

    private SummaryResponseDTO summarize(Long conversationId, Long userId){
        String token = UUID.randomUUID().toString();

        boolean isAcquired = lockService.tryAcquire(conversationId, token, Duration.ofMinutes(2));

        if(!isAcquired){
            throw new SummaryInProgressException("Summary is in progress");
        }

        boolean reserved = false, succeeded = false;

        try {
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
                stringBuilder.append(m.getSender().getName()).append(" ").append(m.getSender().getSurname()).append(":").append(" ").append(m.getMessage()).append("\n");
            }

            if(userSummaryQuotaRepository.subtractOneQuota(userId) == 0){
                throw new BadRequestException("No more quotas left");
            }

            reserved = true;

            String response = geminiSummaryClient.generateSummary(stringBuilder.toString());

            ConversationSummary conversationSummary = new ConversationSummary();

            conversationSummary.setLastMessageId(lastMessage.getId());
            conversationSummary.setSummary(response);
            conversationSummary.setConversationId(conversationId);

            conversationSummaryRepository.save(conversationSummary);

            succeeded = true;

            return summaryMapper.toSummaryResponseDTO(conversationId, response, userSummaryQuotaRepository.findRemainingQuota(userId));
        } catch (ResourceNotFoundException | SummaryGenerationException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new SummaryGenerationException("Couldn't create summary", e);
        } finally {
            try {
                if(reserved && !succeeded){
                    userSummaryQuotaRepository.addOneQuota(userId);
                }
            } finally {
                lockService.release(conversationId, token);
            }
        }
    }

    private SummaryResponseDTO cachedSummary(Long conversationId, String summary, int remainingQuota){
        return summaryMapper.toSummaryResponseDTO(conversationId, summary, remainingQuota);
    }

    private UserSummaryQuota registerUser(Long userId){
        userSummaryQuotaRepository.insertQuotaIfAbsent(userId, LocalDate.now());

        return userSummaryQuotaRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Quota not found"));
    }
}