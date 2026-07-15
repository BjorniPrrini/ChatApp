package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessagePageDTO;
import com.chatappfrontend.frontend.model.MessageResponseDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MessageService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private final static String BASE_URL = AppConfig.get("api.base.url") + "/api/message";

    public MessagePageDTO getMessages(Long conversationId, LocalDateTime before) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + conversationId;
        if(before != null){
            url += "?before=" + before;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), MessagePageDTO.class);
        }else if(response.statusCode() == 401){
            throw new IOException("Unauthorized");
        }else{
            throw new IOException("Failed to load messages: " + response.statusCode());
        }
    }

    public MessageResponseDTO sendMessage(Long conversationId, String message) throws Exception {
        Map<String, Object> body = new HashMap<>();

        body.put("conversationId", conversationId);
        body.put("message", message);

        return postMessage(body);
    }

    public MessageResponseDTO replyMessage(Long conversationId, Long replyToId, String message) throws Exception {
        Map<String, Object> body = new HashMap<>();

        body.put("conversationId", conversationId);
        body.put("message", message);
        body.put("replyToId", replyToId);

        return postMessage(body);
    }

    private MessageResponseDTO postMessage(Map<String, Object> body) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sendMessage"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), MessageResponseDTO.class);
        }else if(response.statusCode() == 401){
            throw new Exception("Unauthorized");
        }else{
            throw new Exception("Failed to send message: " + response.statusCode());
        }
    }
}