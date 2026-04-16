package com.trainchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.trainchatbot.model.IntentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class GeminiService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    public IntentType detectIntent(String userInput) {
        String prompt = "Classify user train query intent as exactly one of: TRAIN_STATUS, TRAIN_ROUTE, TRAIN_TIME.\n"
                + "Input: " + userInput + "\nReturn only the label.";
        String raw = askGemini(prompt);
        IntentType parsed = parseIntent(raw);
        if (parsed == IntentType.UNKNOWN) {
            return fallbackIntent(userInput);
        }
        return parsed;
    }

    public String translate(String sourceText, String targetLanguageCode) {
        if (targetLanguageCode == null || "en".equalsIgnoreCase(targetLanguageCode)) {
            return sourceText;
        }
        String languageName = switch (targetLanguageCode.toLowerCase(Locale.ROOT)) {
            case "ta" -> "Tamil";
            case "hi" -> "Hindi";
            case "te" -> "Telugu";
            case "kn" -> "Kannada";
            case "ml" -> "Malayalam";
            case "bn" -> "Bengali";
            case "gu" -> "Gujarati";
            case "mr" -> "Marathi";
            case "pa" -> "Punjabi";
            case "or" -> "Odia";
            case "ur" -> "Urdu";
            case "fr" -> "French";
            case "de" -> "German";
            case "es" -> "Spanish";
            case "ja" -> "Japanese";
            case "zh" -> "Chinese";
            case "ko" -> "Korean";
            case "ar" -> "Arabic";
            case "ru" -> "Russian";
            default -> "English";
        };
        String prompt = "Translate this train update into " + languageName
                + " keeping station names and time values unchanged:\n" + sourceText;
        String translated = askGemini(prompt);
        if (translated == null || translated.isBlank()) {
            return sourceText;
        }
        return translated.trim();
    }

    private String askGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "";
        }
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel
                + ":generateContent?key=" + geminiApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", new Object[]{
                Map.of("parts", new Object[]{Map.of("text", prompt)})
        });
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(payload, headers), JsonNode.class
            );
            JsonNode body = response.getBody();
            if (body == null) {
                return "";
            }
            JsonNode textNode = body.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            return textNode.isMissingNode() ? "" : textNode.asText();
        } catch (Exception ex) {
            return "";
        }
    }

    private IntentType parseIntent(String raw) {
        if (raw == null) {
            return IntentType.UNKNOWN;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (IntentType type : IntentType.values()) {
            if (normalized.contains(type.name())) {
                return type;
            }
        }
        return IntentType.UNKNOWN;
    }

    private IntentType fallbackIntent(String userInput) {
        String text = userInput == null ? "" : userInput.toLowerCase(Locale.ROOT);
        if (text.contains("route") || text.contains("stations") || text.contains("path")) {
            return IntentType.TRAIN_ROUTE;
        }
        if (text.contains("time") || text.contains("eta") || text.contains("arrival")) {
            return IntentType.TRAIN_TIME;
        }
        if (text.contains("status") || text.contains("where") || text.contains("irukku") || text.contains("कहाँ")) {
            return IntentType.TRAIN_STATUS;
        }
        return IntentType.TRAIN_STATUS;
    }

    /**
     * Generate a smart natural-language reply using Gemini.
     * The prompt must include the target language instructions.
     * Returns cleaned text (no markdown), or empty string on failure.
     */
    public String generateSmartReply(String prompt) {
        String raw = askGemini(prompt);
        if (raw == null || raw.isBlank()) {
            return "";
        }
        // Strip any markdown artifacts Gemini might add
        String cleaned = raw
                .replaceAll("```[a-z]*\\n?", "")
                .replaceAll("```", "")
                .replaceAll("\\*\\*", "")
                .replaceAll("\\*", "")
                .trim();
        return cleaned;
    }
}
