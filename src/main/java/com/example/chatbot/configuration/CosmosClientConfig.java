package com.example.chatbot.configuration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosClientConfig {

    @Bean
    public CosmosAsyncClient cosmosAsyncClient(
            @Value("${spring.ai.vectorstore.cosmosdb.endpoint}") String endpoint,
            @Value("${spring.ai.vectorstore.cosmosdb.key}") String key
    ) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("Missing required property: endpoint");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing required property: key");
        }

        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildAsyncClient();
    }
}
