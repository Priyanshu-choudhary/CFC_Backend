package com.cfc.platform.DTO;

/**
 * Incoming request body for the AI chat endpoint.
 *
 * First message:  provide problemId + prompt (sessionId must be null/blank)
 * Follow-up:      provide sessionId + prompt (problemId is ignored)
 *
 * Optional fields currentCode / language give the AI context about what the
 * user is currently typing in the editor.
 */
public record ChatRequest(
    String sessionId,
    String problemId,         // Keeping for your old /chat endpoint if needed
    String problemNumber,     // New field
    String problemStatement,  // New field
    String currentLanguage,   // Standardized to currentLanguage
    String currentCode,
    String prompt,
    String language
) {}