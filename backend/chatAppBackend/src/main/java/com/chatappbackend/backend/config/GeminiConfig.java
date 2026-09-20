package com.chatappbackend.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {
    @Value("${ai.api.key}")
    private String geminiApi;

    @Bean
    public RestClient aiConnect(){
        return RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", geminiApi)
                .build();
    }
}