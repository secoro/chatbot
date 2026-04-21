package com.example.chatbot.services;

import com.example.chatbot.dtos.ConversationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.stereotype.Service;

import static com.example.chatbot.configuration.ChatClientConfig.ACCOUNT_ID_CONTEXT_KEY;

@RequiredArgsConstructor
@Service
public class ChatService {

    private static final String BASE_SYSTEM_PROMPT = """
            You are a specialist in mobility exercises, habit creation and mental health improvement.
            Your name is SMYM — introduce yourself as such if asked.
            
            When information is missing, use your own knowledge to provide the best helpful response.
            Never say "I don't know" if you can reasonably infer or explain the answer.
            If retrieval context conflicts with general knowledge, prefer the retrieval context.
            """;

    private final ChatClient chatClient;

    public String chat(ConversationDto conversationDto) {
        return chatClient.prompt(conversationDto.userMessage())
                .system(BASE_SYSTEM_PROMPT)
                .advisors(a -> a.param(ACCOUNT_ID_CONTEXT_KEY, conversationDto.userId()))
                .call()
                .content();
    }
}
