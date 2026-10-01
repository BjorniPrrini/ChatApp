package com.chatappbackend.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GeminiConfig {
    @Value("${ai.api.key}")
    private String geminiApi;

    @Bean
    public RestClient aiConnect(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(5));
        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(30));

        return RestClient.builder()
                .baseUrl(getBaseUrl())
                .defaultHeader("x-goog-api-key", geminiApi)
                .requestFactory(simpleClientHttpRequestFactory)
                .build();
    }

    @Bean
    public RestClient aiSummary(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofMinutes(1));
        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(getBaseUrl())
                .defaultHeader("x-goog-api-key", geminiApi)
                .requestFactory(simpleClientHttpRequestFactory)
                .build();
    }

    private String getBaseUrl(){
        return "https://generativelanguage.googleapis.com/v1beta";
    }
}