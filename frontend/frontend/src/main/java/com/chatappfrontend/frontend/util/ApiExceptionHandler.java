package com.chatappfrontend.frontend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Response;
import okhttp3.ResponseBody;

import java.net.http.HttpResponse;

public class ApiExceptionHandler {
    private static final ObjectMapper objectMapper = JsonMapper.get();

    public static void handle(HttpResponse<String> response) throws Exception {
        String message = extractMessage(response.body());

        switch(response.statusCode()){
            case 400, 404, 403:
                throw new Exception(message);
            case 401:
                throw new Exception("Session expired. Please login again.");
            default:
                throw new Exception("Request failed: " + response.statusCode());
        }
    }

    public static void handle(Response response) throws Exception {
        String body = "";

        try (ResponseBody responseBody = response.body()) {
            if(responseBody != null){
                body = responseBody.string();
            }
        }

        String message = extractMessage(body);

        switch(response.code()){
            case 400, 404, 403:
                throw new Exception(message);
            case 401:
                throw new Exception("Session expired. Please login again.");
            default:
                throw new Exception("Request failed: " + response.code());
        }
    }

    private static String extractMessage(String body){
        try {
            JsonNode node = objectMapper.readTree(body);

            if(node.has("message")){
                return node.get("message").asText();
            }

            return body;
        } catch (Exception e) {
            return body;
        }
    }
}