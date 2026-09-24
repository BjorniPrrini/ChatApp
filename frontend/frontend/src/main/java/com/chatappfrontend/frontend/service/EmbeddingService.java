package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessageSearchResultResponseDTO;
import com.chatappfrontend.frontend.util.ApiExceptionHandler;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class EmbeddingService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/embedding";

    public List<MessageSearchResultResponseDTO> searchMessages(String query) throws Exception {
        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/messageSearchResult?query=" + encodedQuery))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, MessageSearchResultResponseDTO.class));
        }

        ApiExceptionHandler.handle(response);

        return null;
    }
}