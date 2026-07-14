package com.cfc.platform.controller;

import com.cfc.platform.DTO.ChatRequest;
import com.cfc.platform.DTO.ChatResponse;
import com.cfc.platform.Service.AIService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI-assisted problem solving.
 *
 * POST /api/ai/chat
 *
 * First request:
 *   { "problemId": "abc123", "prompt": "Explain this problem" }
 *   → { "sessionId": "<uuid>", "response": "..." }
 *
 * Follow-up request:
 *   { "sessionId": "<uuid>", "prompt": "What if I start with BFS?" }
 *   → { "sessionId": "<uuid>", "response": "..." }
 *
 * Optionally include currentCode + language on any request for richer context.
 *
 * Authentication: JWT required (handled by JwtFilter / Spring Security).
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

@PostMapping("/chat")
public ResponseEntity<ChatResponse> handleChat(@RequestBody ChatRequest request) {
    // Validate: must have either a session (follow-up) or a problem (new session)
    boolean hasSession = request.sessionId() != null && !request.sessionId().isBlank();
    boolean hasProblem = request.problemId() != null && !request.problemId().isBlank();

    if (!hasSession && !hasProblem) {
        return ResponseEntity.badRequest().build();
    }

    if (request.prompt() == null || request.prompt().isBlank()) {
        return ResponseEntity.badRequest().build();
    }
    System.out.println(request);
    ChatResponse response = aiService.processChatMessage(request);
    return ResponseEntity.ok(response);
}

@PostMapping("/fullChat")
public ResponseEntity<ChatResponse> handleFullChat(@RequestBody ChatRequest request) {
    // Validate: Must have a prompt
    if (request.prompt() == null || request.prompt().isBlank()) {
        return ResponseEntity.badRequest().build();
    }

    // Validate: Must have either an existing session ID OR the initial problem statement
    boolean hasSession = request.sessionId() != null && !request.sessionId().isBlank();
    boolean hasProblem = request.problemStatement() != null && !request.problemStatement().isBlank();

    if (!hasSession && !hasProblem) {
        return ResponseEntity.badRequest().build();
    }
    
    ChatResponse response = aiService.processFullChatMessage(request);
    return ResponseEntity.ok(response);
}
}