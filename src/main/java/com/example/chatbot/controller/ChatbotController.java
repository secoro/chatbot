package com.example.chatbot.controller;

import com.example.chatbot.dtos.ConversationDto;
import com.example.chatbot.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatbotController {

    private final ChatService chatService;

    @GetMapping("/chat")
    public ResponseEntity<String> index(@RequestBody ConversationDto conversationDto) {
        log.info("Initiating chat with user");
        String response = chatService.chat(conversationDto);
        return ResponseEntity.ok(response);
    }
}
