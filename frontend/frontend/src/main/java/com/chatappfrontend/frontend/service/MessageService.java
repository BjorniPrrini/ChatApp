package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessagePageDTO;
import com.chatappfrontend.frontend.model.MessageResponseDTO;
import com.chatappfrontend.frontend.util.ApiExceptionHandler;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    public MessagePageDTO getMessages(Long conversationId, LocalDateTime before) throws Exception {
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
        }

        ApiExceptionHandler.handle(response);

        return null;
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
        }

        ApiExceptionHandler.handle(response);

        return null;
    }

    public MessageResponseDTO editMessage(Long messageId, String newMessage) throws Exception {
        Map<String, Object> body = new HashMap<>();

        body.put("newMessage", newMessage);

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + messageId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), MessageResponseDTO.class);
        }

        ApiExceptionHandler.handle(response);

        return null;
    }

    public void deleteMessage(Long messageId, String scope) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + messageId + "/" + scope))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }

        ApiExceptionHandler.handle(response);
    }

    public void deleteMessageForMe(Long messageId) throws Exception {
        deleteMessage(messageId, "me");
    }

    public void deleteMessageForEveryone(Long messageId) throws Exception {
        deleteMessage(messageId, "everyone");
    }
}