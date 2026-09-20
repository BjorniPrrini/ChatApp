package com.chatappbackend.backend.service.embedding;

import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.entity.MessageEmbedding;
import com.chatappbackend.backend.exception.EmbeddingException;
import com.chatappbackend.backend.repository.MessageEmbeddingRepository;
import com.chatappbackend.backend.repository.MessageRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmbeddingSweepService {
    private final MessageRepository messageRepository;
    private final EmbeddingService embeddingService;
    private final MessageEmbeddingRepository messageEmbeddingRepository;

    public EmbeddingSweepService(MessageRepository messageRepository, EmbeddingService embeddingService, MessageEmbeddingRepository messageEmbeddingRepository) {
        this.messageRepository = messageRepository;
        this.embeddingService = embeddingService;
        this.messageEmbeddingRepository = messageEmbeddingRepository;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void retryEmbeddings(){
        List<Message> messages = messageRepository.findMessageWithNoEmbedding();

        for (Message message : messages) {
            try {
                float[] embedding = embeddingService.messageToVector(message.getMessage());

                MessageEmbedding messageEmbedding = new MessageEmbedding();

                messageEmbedding.setEmbedding(embedding);
                messageEmbedding.setMessage(message);

                messageEmbeddingRepository.save(messageEmbedding);
            } catch (EmbeddingException _) {

            }
        }
    }
}