package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.SummaryResponseDTO;
import com.chatappfrontend.frontend.util.ApiExceptionHandler;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SummaryService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/summary";

    public SummaryResponseDTO getSummary(Long conversationId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + conversationId))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .timeout(Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), SummaryResponseDTO.class);
        }

        ApiExceptionHandler.handle(response);

        return null;
    }
}