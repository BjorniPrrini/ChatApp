package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.FriendResponseDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FriendService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/friends";

    public List<FriendResponseDTO> getFriends() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/friends"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, FriendResponseDTO.class));
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

    public List<FriendResponseDTO> getFriendRequests() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/requests"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, FriendResponseDTO.class));
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

    public List<FriendResponseDTO> getSuggestions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/suggestions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, FriendResponseDTO.class));
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

    public void sendFriendRequest(Long receiverId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/send/" + receiverId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
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

    public void acceptFriendRequest(Long senderId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/accept/" + senderId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
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

    public void rejectFriendRequest(Long senderId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reject/" + senderId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
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

    public List<FriendResponseDTO> getSentRequests() throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sent"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, FriendResponseDTO.class));
        }else{
            throw new Exception("Failed to get sent requests: " + response.statusCode());
        }
    }

    public void removeFriend(Long userId, Long friendId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/remove/" + friendId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
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

    public void blockFriend(Long friendId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.get("api.base.url") + "/api/block/" + friendId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
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

    public void unblockUser(Long friendId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.get("api.base.url") + "/api/block/" + friendId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }else if(response.statusCode() == 401){
            throw new Exception("Unauthorized - please login again");
        }else{
            throw new Exception("Request failed: " + response.statusCode());
        }
    }

    public List<FriendResponseDTO> getBlockedUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.get("api.base.url") + "/api/block/getBlocked"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, FriendResponseDTO.class));
        }else{
            throw new Exception("Failed to get blocked users: " + response.statusCode());
        }
    }
}