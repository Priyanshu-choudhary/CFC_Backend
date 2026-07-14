package com.cfc.platform.ConfigSecurity;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration (Spring AI 2.0.0).
 *
 * In 2.0.0 the ChatMemory abstraction was split into:
 *   • ChatMemoryRepository  – raw storage (InMemoryChatMemoryRepository for dev/test)
 *   • ChatMemory            – windowed view over the repository (MessageWindowChatMemory)
 *
 * For production swap InMemoryChatMemoryRepository for a Redis/MongoDB-backed repository.
 *
 * The conversation ID is passed at call-time via the param key
 * "chat_memory_conversation_id" (see AIService).
 */
@Configuration
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
            You are an expert competitive programming and DSA (Data Structures & Algorithms) \
            tutor embedded in a coding platform.

            Your role is to GUIDE the user towards the solution — not to give it away directly.
            Follow these principles:
            - Explain concepts clearly with examples when asked.
            - If the user shares code, point out bugs and suggest fixes without rewriting the whole thing.
            - Suggest algorithmic approaches (BFS, DP, two-pointer, etc.) when asked.
            - Ask Socratic questions to help the user think through edge cases.
            - Use proper markdown formatting: code fences for code blocks, bullet lists for steps.
            - Be concise, friendly, and encouraging.
            - If the user asks for the full solution, give a well-commented version and explain it.
            """;

    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)   // keep last 20 messages per session
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }
}