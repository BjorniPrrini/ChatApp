package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.ConversationDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ConversationService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/conversation";

    public List<ConversationDTO> getConversations() throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getUserConversations"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, ConversationDTO.class));
        }else if(response.statusCode() == 401){
            throw new Exception("Unauthorized - please login again");
        }else if(response.statusCode() == 403){
            throw new Exception("Forbidden");
        }else if(response.statusCode() == 404){
            throw new Exception("Not found");
        }else{
            throw new Exception("Request failed: " + response.statusCode());
        }
    }
}