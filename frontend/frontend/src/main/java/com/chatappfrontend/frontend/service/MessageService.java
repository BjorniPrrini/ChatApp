package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessagePageDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class MessageService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
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

    public void sendMessage(Long conversationId, String message) throws Exception {
        String body = String.format("{\"conversationId\":%d,\"message\":\"%s\"}", conversationId, message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sendMessage"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }else if (response.statusCode() == 401){
            throw new Exception("Unauthorized");
        }else{
            throw new Exception("Failed to send message: " + response.statusCode());
        }
    }
}