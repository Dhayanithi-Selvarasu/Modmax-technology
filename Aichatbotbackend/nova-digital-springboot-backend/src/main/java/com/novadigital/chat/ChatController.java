package com.novadigital.chat;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final GeminiService geminiService;
    private final ChatConversationRepository repository;

    public ChatController(GeminiService geminiService, ChatConversationRepository repository) {
        this.geminiService = geminiService;
        this.repository = repository;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request,
                             @RequestHeader(value="X-Chat-Session", required=false) String session) {
        String reply = geminiService.reply(request);
        String sessionId = (session == null || session.isBlank()) ? UUID.randomUUID().toString() : session;
        repository.save(new ChatConversation(sessionId, request.message(), reply));
        return new ChatResponse(reply);
    }
}
