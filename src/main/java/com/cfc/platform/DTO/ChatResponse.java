package com.cfc.platform.DTO;

/**
 * Response returned to the frontend after every AI chat call.
 *
 * sessionId – client must store this and echo it back on follow-up requests
 * response  – the raw text content from the AI model
 */
public record ChatResponse(
        String sessionId,
        String response
) {}