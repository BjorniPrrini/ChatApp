package com.chatappbackend.backend.service.embedding;

import com.chatappbackend.backend.dto.embedding.Content;
import com.chatappbackend.backend.dto.embedding.EmbeddingRequestDTO;
import com.chatappbackend.backend.dto.embedding.EmbeddingResponseDTO;
import com.chatappbackend.backend.dto.embedding.Part;
import com.chatappbackend.backend.dto.message.MessageSearchResultDTO;
import com.chatappbackend.backend.entity.Message;
import com.chatappbackend.backend.exception.EmbeddingException;
import com.chatappbackend.backend.mapper.MessageMapper;
import com.chatappbackend.backend.repository.MessageEmbeddingRepository;
import com.chatappbackend.backend.repository.MessageRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
public class EmbeddingServiceImpl implements EmbeddingService{
    private final RestClient restClient;
    private final MessageRepository messageRepository;
    private final MessageEmbeddingRepository messageEmbeddingRepository;
    private final MessageMapper messageMapper;

    public EmbeddingServiceImpl(RestClient restClient, MessageRepository messageRepository, MessageEmbeddingRepository messageEmbeddingRepository, MessageMapper messageMapper) {
        this.restClient = restClient;
        this.messageRepository = messageRepository;
        this.messageEmbeddingRepository = messageEmbeddingRepository;
        this.messageMapper = messageMapper;
    }

    @Override
    public float[] messageToVector(String message) throws EmbeddingException {
        EmbeddingRequestDTO request = new EmbeddingRequestDTO();

        request.setContent(new Content(List.of(new Part(message))));
        request.setModel("models/gemini-embedding-001");
        request.setOutputDimensionality(768);

        try {
            List<Float> values = Objects.requireNonNull(restClient.post()
                            .uri("/models/gemini-embedding-001:embedContent")
                            .body(request)
                            .retrieve()
                            .body(EmbeddingResponseDTO.class))
                    .getEmbedding()
                    .getValues();

            float[] result = new float[values.size()];

            for(int i = 0; i < values.size(); i++){
                result[i] = values.get(i);
            }

            return result;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embedding", e);
        }
    }

    @Override
    public List<MessageSearchResultDTO> searchMessages(Long userId, String query, int limit) throws EmbeddingException {
        List<Message> list = messageRepository.findAllById(messageEmbeddingRepository.searchMessageByVector(userId, toVectorString(messageToVector(query)), limit));

        return list.stream()
                .map(messageMapper::toMessageSearchResultDTO)
                .toList();
    }

    private String toVectorString(float[] embedding){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[");

        for(int i = 0; i < embedding.length; i++){
            stringBuilder.append(embedding[i]);

            if(i < embedding.length - 1){
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}