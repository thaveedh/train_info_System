package com.trainchatbot.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * NlpService
 * 
 * This service communicates with the Python FastAPI NLP service.
 * It sends the user's message to detect language, intent, and train number.
 */
@Service
public class NlpService {

    private final String NLP_URL = "http://localhost:8000/process";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> analyzeMessage(String message) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("message", message);
            return restTemplate.exchange(
                NLP_URL,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
        } catch (Exception e) {
            // Fallback NLP logic if Python service is down
            // NOTE: Cannot use Map.of() here because trainNumber may be null
            // and Map.of() throws NullPointerException on null values.
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("intent", "UNKNOWN");
            fallback.put("trainNumber", extractTrainNumber(message));
            fallback.put("language", "en");
            return fallback;
        }
    }

    private String extractTrainNumber(String msg) {
        // Simple regex fallback for 5 digits
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{5}");
        java.util.regex.Matcher matcher = pattern.matcher(msg);
        return matcher.find() ? matcher.group() : null;
    }

    public String generateFinalResponse(String rawData, String language) {
        String url = "http://localhost:8000/respond";
        try {
            Map<String, String> request = Map.of("data", rawData, "language", language);
            Map<String, String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<Map<String, String>>() {}
            ).getBody();
            return response.get("response");
        } catch (Exception e) {
            String trimmed = rawData == null ? "" : rawData.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return trimmed;
            }
            return "Unable to generate AI response. Train Details: " + rawData;
        }
    }
}
