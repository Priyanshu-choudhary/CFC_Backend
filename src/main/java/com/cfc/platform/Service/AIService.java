package com.cfc.platform.Service;

import com.cfc.platform.DTO.ChatRequest;
import com.cfc.platform.DTO.ChatResponse;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.Pojo.Posts.Posts;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates every interaction with the AI model.
 *
 * Flow:
 *  ① First message  – no sessionId yet.
 *    - Generate a new sessionId (UUID).
 *    - Fetch the problem from MongoDB and build a rich context prompt.
 *    - The context is prepended to the user's first message so the model
 *      understands the problem immediately.
 *
 *  ② Follow-up messages – sessionId already exists.
 *    - The MessageChatMemoryAdvisor automatically loads the conversation
 *      history stored in InMemoryChatMemory keyed by sessionId.
 *    - The user's follow-up is appended and GPT replies in context.
 */
@Service
public class AIService {

    private final ChatClient chatClient;
    private final PostRepo postRepo;

    public AIService(ChatClient chatClient, PostRepo postRepo) {
        this.chatClient = chatClient;
        this.postRepo = postRepo;
    }

    public ChatResponse processChatMessage(ChatRequest request) {

        // ── Step 1: Resolve session ID ─────────────────────────────────────
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : UUID.randomUUID().toString();

        // ── Step 2: Build the user prompt ──────────────────────────────────
        String userPrompt;

        boolean isFirstMessage = (request.sessionId() == null || request.sessionId().isBlank());

        if (isFirstMessage && request.problemId() != null && !request.problemId().isBlank()) {
            // Fetch the real problem from MongoDB and inject it as context
            String problemContext = fetchProblemContext(request.problemId());

            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("=== CODING PROBLEM CONTEXT ===\n");
            promptBuilder.append(problemContext);
            promptBuilder.append("\n\n");

            // Optionally include the user's current editor code
            if (request.currentCode() != null && !request.currentCode().isBlank()) {
                promptBuilder.append("=== USER'S CURRENT CODE (")
                             .append(request.language() != null ? request.language() : "unknown language")
                             .append(") ===\n");
                promptBuilder.append(request.currentCode());
                promptBuilder.append("\n\n");
            }

            promptBuilder.append("=== USER QUESTION ===\n");
            promptBuilder.append(request.prompt());

            userPrompt = promptBuilder.toString();
        } else {
            // Follow-up message — optionally attach current code snapshot
            if (request.currentCode() != null && !request.currentCode().isBlank()) {
                userPrompt = String.format(
                    "My current code (%s):\n%s\n\n%s",
                    request.language() != null ? request.language() : "unknown",
                    request.currentCode(),
                    request.prompt()
                );
            } else {
                userPrompt = request.prompt();
            }
        }

        // ── Step 3: Call Spring AI with the conversation memory advisor ────
        // In Spring AI 2.0.0 there is no CONVERSATION_ID static constant.
        // The key used internally by BaseChatMemoryAdvisor is the string
        // "chat_memory_conversation_id" — pass it explicitly here.
        String aiContent = this.chatClient.prompt()
                .user(userPrompt)
                .advisors(spec -> spec.param(
                        "chat_memory_conversation_id",
                        sessionId
                ))
                .call()
                .content();

        // ── Step 4: Return our own DTO (not the Spring AI internal type) ───
        return new ChatResponse(sessionId, aiContent);
    }

public ChatResponse processFullChatMessage(ChatRequest request) {
    // 1. Resolve or spin up a new session ID
    String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
            ? request.sessionId()
            : UUID.randomUUID().toString();

    // 2. Determine if this is the very first message based on whether a problem statement was provided
    boolean isFirstMessage = request.problemStatement() != null && !request.problemStatement().isBlank();
    String userPrompt;

    if (isFirstMessage) {
        // First turn payload: Inject problem context explicitly
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("=== LEETCODE PROBLEM CONTEXT ===\n");
        if (request.problemNumber() != null && !request.problemNumber().isBlank()) {
            promptBuilder.append("Problem Number: ").append(request.problemNumber()).append("\n");
        }
        promptBuilder.append(request.problemStatement()).append("\n\n");

        if (request.currentCode() != null && !request.currentCode().isBlank()) {
            promptBuilder.append("=== USER'S CURRENT CODE (")
                         .append(request.currentLanguage() != null ? request.currentLanguage() : "unknown language")
                         .append(") ===\n");
            promptBuilder.append(request.currentCode()).append("\n\n");
        }

        promptBuilder.append("=== USER QUESTION ===\n");
        promptBuilder.append(request.prompt());
        userPrompt = promptBuilder.toString();
    } else {
        // Follow-up payload: Keep it light, just ship the updated code canvas + the next user question
        if (request.currentCode() != null && !request.currentCode().isBlank()) {
            userPrompt = String.format(
                "My current code (%s):\n%s\n\n%s",
                request.currentLanguage() != null ? request.currentLanguage() : "unknown",
                request.currentCode(),
                request.prompt()
            );
        } else {
            userPrompt = request.prompt();
        }
    }

    // 3. Fire to Spring AI. The 'chat_memory_conversation_id' pulls the previous 
    // full problem context automatically from memory history!
    String aiContent = this.chatClient.prompt()
            .user(userPrompt)
            .advisors(spec -> spec.param("chat_memory_conversation_id", sessionId))
            .call()
            .content();

    return new ChatResponse(sessionId, aiContent);
}

    // ── Helper: load problem details from MongoDB ──────────────────────────

    /**
     * Fetches the problem document from the Posts collection and converts
     * the relevant fields into a plain-text context block for the AI.
     */
    private String fetchProblemContext(String problemId) {
        Optional<Posts> postOpt = postRepo.findById(problemId);

        if (postOpt.isEmpty()) {
            return "Problem ID: " + problemId + "\n(No additional details found — answer generically.)";
        }

        Posts problem = postOpt.get();
        StringBuilder ctx = new StringBuilder();

        if (problem.getTitle() != null) {
            ctx.append("Title: ").append(problem.getTitle()).append("\n");
        }
        if (problem.getDifficulty() != null) {
            ctx.append("Difficulty: ").append(problem.getDifficulty()).append("\n");
        }
        if (problem.getDescription() != null) {
            ctx.append("\nProblem Statement:\n").append(problem.getDescription()).append("\n");
        }
        if (problem.getExample() != null) {
            ctx.append("\nExample:\n").append(problem.getExample()).append("\n");
        }
        if (problem.getConstrain() != null) {
            ctx.append("\nConstraints:\n").append(problem.getConstrain()).append("\n");
        }
        if (problem.getTimecomplixity() != null) {
            ctx.append("\nExpected Time Complexity: ").append(problem.getTimecomplixity()).append("\n");
        }

        return ctx.toString();
    }
}