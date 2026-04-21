package com.example.chatbot.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ConversationDto(@JsonProperty("session_id") UUID sessionId,
                              @JsonProperty("user_id") UUID userId,
                              @JsonProperty("user_message") String userMessage) {
}
