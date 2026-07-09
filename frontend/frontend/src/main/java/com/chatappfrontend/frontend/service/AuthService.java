package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.AuthResponseDTO;
import com.chatappfrontend.frontend.util.AppConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/auth";

    public AuthResponseDTO login(String email, String password) throws Exception{
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200){
            return objectMapper.readValue(response.body(), AuthResponseDTO.class);
        }else{
            throw new Exception("Login failed: " + response.body());
        }
    }

    public AuthResponseDTO register(String name, String surname, String email, String password, String confirmPassword, String nickname, String phoneNumber) throws Exception{
        String body = String.format(
                "{\"name\":\"%s\",\"surname\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\",\"nickname\":\"%s\",\"phoneNumber\":\"%s\"}",
                name, surname, email, password, confirmPassword,
                nickname != null ? nickname : "",
                phoneNumber != null ? phoneNumber : ""
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200){
            return objectMapper.readValue(response.body(), AuthResponseDTO.class);
        }else{
            throw new Exception("Register failed: " + response.body());
        }
    }
}