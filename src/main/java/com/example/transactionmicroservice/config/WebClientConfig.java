package com.example.transactionmicroservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient.
 */
@Configuration
public class WebClientConfig {

    /**
     * Base URL for the Account Microservice, injected from properties.
     */
    @Value("${microservices.bank-accounts.base-url}")
    private String bankAccountsBaseUrl;

    /**
     * Creates a WebClient bean with a base URL for the Account Microservice.
     *
     * @return A configured WebClient instance.
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(bankAccountsBaseUrl) // Base URL for the Account Microservice
                .build();
    }
}
