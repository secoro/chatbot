package com.example.chatbot.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
public class ChatClientConfig {

    private static final boolean DEFAULT_ENABLE_MEMORY = true;
    private static final boolean DEFAULT_ENABLE_LOGGING = true;
    private static final boolean DEFAULT_ENABLE_RAG_FILES = true;
    private static final boolean DEFAULT_ENABLE_RAG_CHUNKS = true;

    public static final String ACCOUNT_ID_CONTEXT_KEY = "accountId";

    @Bean
    public ChatClient chatClient(
            ChatModel chatModel,
            @Qualifier("filesVectorStore") VectorStore filesVectorStore,
            @Qualifier("chunksVectorStore") VectorStore chunksVectorStore
    ) {
        List<Advisor> advisors = getAdvisors(filesVectorStore, chunksVectorStore);

        return ChatClient.builder(chatModel)
                .defaultAdvisors(advisors)
                .build();
    }

    private List<Advisor> getAdvisors(VectorStore filesVectorStore, VectorStore chunksVectorStore) {
        List<Advisor> advisors = new ArrayList<>();

        if (DEFAULT_ENABLE_MEMORY) {
            MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
            MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build();
            advisors.add(messageChatMemoryAdvisor);
        }
        if (DEFAULT_ENABLE_LOGGING) {
            SimpleLoggerAdvisor simpleLoggerAdvisor = SimpleLoggerAdvisor.builder().build();
            advisors.add(simpleLoggerAdvisor);
        }
        if (DEFAULT_ENABLE_RAG_FILES || DEFAULT_ENABLE_RAG_CHUNKS) {
            RetrievalAugmentationAdvisor multiSourceRagAdvisor = createMultiSourceRagAdvisor(filesVectorStore, chunksVectorStore);
            advisors.add(multiSourceRagAdvisor);
        }

        return advisors;
    }

    private RetrievalAugmentationAdvisor createMultiSourceRagAdvisor(VectorStore filesVectorStore,
                                                                     VectorStore chunksVectorStore) {

        DocumentRetriever multiSourceRetriever = query -> {

            List<Document> filesDocs = new ArrayList<>();
            List<Document> chunksDocs = new ArrayList<>();

            if (DEFAULT_ENABLE_RAG_FILES) {
                String accountId = query.context().get(ACCOUNT_ID_CONTEXT_KEY).toString();
                if (StringUtils.isBlank(accountId)) {
                    throw new IllegalArgumentException("Missing required context key: '%s'".formatted(ACCOUNT_ID_CONTEXT_KEY));
                }

                DocumentRetriever filesRetriever = VectorStoreDocumentRetriever.builder()
                        .vectorStore(filesVectorStore)
                        .similarityThreshold(0.0)
                        .topK(5)
                        .filterExpression(new FilterExpressionBuilder()
                                .eq(ACCOUNT_ID_CONTEXT_KEY, accountId)
                                .build())
                        .build();

                filesDocs = filesRetriever.retrieve(query);
            }

            if (DEFAULT_ENABLE_RAG_CHUNKS) {
                DocumentRetriever chunksRetriever = VectorStoreDocumentRetriever.builder()
                        .vectorStore(chunksVectorStore)
                        .similarityThreshold(0.5)
                        .topK(5)
                        .build();

                chunksDocs = chunksRetriever.retrieve(query);
            }

            log.info("Retrieved {} files and {} chunks documents for query: '{}'", filesDocs.size(), chunksDocs.size(), query.text());

            // Combine with deduplication (keep the first occurrence)
            List<Document> combined = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();

            filesDocs.stream().filter(doc -> seenIds.add(doc.getId())).forEach(combined::add);
            chunksDocs.stream().filter(doc -> seenIds.add(doc.getId())).forEach(combined::add);

            return combined;
        };

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(multiSourceRetriever)
                .documentJoiner(new ConcatenationDocumentJoiner())
                .build();
    }
}
