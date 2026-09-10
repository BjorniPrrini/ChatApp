package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.UserRequestDTO;
import com.chatappfrontend.frontend.util.ApiExceptionHandler;
import com.chatappfrontend.frontend.model.UserResponseDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.List;

public class UserService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.get();
    private static final String BASE_URL = AppConfig.get("api.base.url") + "/api/users";
    private final OkHttpClient okHttpClient = new OkHttpClient();

    public List<UserResponseDTO> searchUsers(String searchTerm) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/searchUsers?searchTerm=" + java.net.URLEncoder.encode(searchTerm, java.nio.charset.StandardCharsets.UTF_8)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponseDTO.class));
        }

        ApiExceptionHandler.handle(response);

        return null;
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) throws Exception {
        String body = String.format("{\"oldPassword\":\"%s\",\"newPassword\":\"%s\",\"confirmedPassword\":\"%s\"}", oldPassword, newPassword, confirmPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/change-password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }

        ApiExceptionHandler.handle(response);
    }

    public UserResponseDTO getUserInformation() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return objectMapper.readValue(response.body(), UserResponseDTO.class);
        }

        ApiExceptionHandler.handle(response);

        return null;
    }

    public void editUserProfile(String name, String surname, String nickname, String phoneNumber) throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO();

        requestDTO.setName(name);
        requestDTO.setSurname(surname);
        requestDTO.setNickname(nickname);
        requestDTO.setPhoneNumber(phoneNumber);

        String body = objectMapper.writeValueAsString(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/updateProfile"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 200 && response.statusCode() < 300){
            return;
        }

        ApiExceptionHandler.handle(response);
    }

    public UserResponseDTO updateProfilePicture(File file) throws Exception {
        String mimeType = Files.probeContentType(file.toPath());

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/profile-picture")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .put(body)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if(response.isSuccessful()){
                return objectMapper.readValue(response.body().string(), UserResponseDTO.class);
            }

            ApiExceptionHandler.handle(response);
        }

        return null;
    }
}