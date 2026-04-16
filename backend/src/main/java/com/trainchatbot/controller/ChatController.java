package com.trainchatbot.controller;

import com.trainchatbot.service.ChatService;
import com.trainchatbot.model.ChatRequest;
import com.trainchatbot.model.ChatResponse;
import com.trainchatbot.model.LiveTrainTrackingJsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ChatController
 *
 * UI layer entry point:
 * - Accepts text or voice payload
 * - Delegates orchestration to ChatService
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Allow React to call this API
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        return chatService.processChat(request);
    }

    @GetMapping("/train/{trainNumber}")
    public ResponseEntity<LiveTrainTrackingJsonResponse> getTrainDetails(@PathVariable String trainNumber) {
        LiveTrainTrackingJsonResponse body = chatService.fetchLiveTrainDetails(trainNumber);
        if (body == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(body);
    }
}
