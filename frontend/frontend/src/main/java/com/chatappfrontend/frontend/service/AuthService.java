package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.AuthResponseDTO;
import com.chatappfrontend.frontend.util.AppConfig;

import com.chatappfrontend.frontend.util.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/auth";

    public AuthResponseDTO login(String email, String password) throws Exception{
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), AuthResponseDTO.class);
        }else if(response.statusCode() == 401){
            throw new Exception("Invalid email or password");
        }else if(response.statusCode() == 404){
            throw new Exception("User not found");
        }else{
            throw new Exception("Login failed: " + response.statusCode());
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

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), AuthResponseDTO.class);
        }else if(response.statusCode() == 400){
            throw new Exception("Email already in use");
        }else if(response.statusCode() == 409){
            throw new Exception("User already exists");
        }else{
            throw new Exception("Registration failed: " + response.statusCode());
        }
    }

    public void forgotPassword(String email) throws Exception{
        String body = String.format("{\"email\":\"%s\"}", email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }else if(response.statusCode() == 404){
            throw new Exception("Email not found");
        }else{
            throw new Exception("Failed to send reset code: " + response.statusCode());
        }
    }

    public void resetPassword(String email, String token, String newPassword, String confirmPassword) throws Exception{
        String body = String.format(
                "{\"email\":\"%s\",\"token\":\"%s\",\"newPassword\":\"%s\",\"confirmPassword\":\"%s\"}",
                email, token, newPassword, confirmPassword
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reset-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("RESET PASSWORD RESPONSE: status=" + response.statusCode() + " body=" + response.body());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }else if(response.statusCode() == 400){
            throw new Exception("Invalid or expired token");
        }else if(response.statusCode() == 404){
            throw new Exception("User not found");
        }else{
            throw new Exception("Failed to reset password: " + response.statusCode());
        }
    }
}