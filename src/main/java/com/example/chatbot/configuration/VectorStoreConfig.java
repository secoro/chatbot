package com.example.chatbot.configuration;

import com.azure.cosmos.CosmosAsyncClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.cosmosdb.CosmosDBVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VectorStoreConfig {

    @Bean("filesVectorStore")
    public VectorStore filesVectorStore(CosmosAsyncClient cosmosClient, EmbeddingModel embeddingModel) {
        return CosmosDBVectorStore.builder(cosmosClient, embeddingModel)
                .databaseName("rag-knowledge-base")
                .containerName("files")
                .metadataFields(List.of("accountId"))
                .build();
    }

    @Bean("chunksVectorStore")
    public VectorStore chunksVectorStore(CosmosAsyncClient cosmosClient, EmbeddingModel embeddingModel) {
        return CosmosDBVectorStore.builder(cosmosClient, embeddingModel)
                .databaseName("rag-knowledge-base")
                .containerName("chunks")
                .metadataFields(List.of("topic", "subtopic"))
                .build();
    }
}
