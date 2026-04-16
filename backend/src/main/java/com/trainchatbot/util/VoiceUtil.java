package com.trainchatbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * VoiceUtil
 *
 * - voice to text: local placeholder for text-based testing
 * - text to voice: OpenAI TTS when configured, graceful fallback otherwise
 */
@Component
public class VoiceUtil {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.tts.model:gpt-4o-mini-tts}")
    private String openAiTtsModel;

    @Value("${openai.tts.voice:alloy}")
    private String defaultVoice;

    public String voiceToText(String voiceInputBase64) {
        if (voiceInputBase64 == null || voiceInputBase64.isBlank()) {
            return "";
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(voiceInputBase64), StandardCharsets.UTF_8);
            if (decoded.startsWith("TEXT:")) {
                return decoded.substring(5).trim();
            }
            return "";
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private final String PYTHON_SERVICE_URL = "http://localhost:8000";

    public String textToVoiceBase64(String text, String language) {
        if (text == null || text.isBlank()) {
            return null;
        }

        // 1. Try OpenAI if configured
        String generated = generateWithOpenAi(text, language);
        if (generated != null && !generated.isBlank()) {
            return generated;
        }

        // 2. Alternate Way: Try Python gTTS for better native quality (especially for Tamil)
        String pythonAudio = generateWithPythonGtts(text, language);
        if (pythonAudio != null && !pythonAudio.isBlank()) {
            return pythonAudio;
        }

        // If no TTS audio could be generated, do not return invalid raw text bytes as audio.
        // Let the frontend fall back to browser speech synthesis instead.
        return null;
    }

    private String generateWithPythonGtts(String text, String language) {
        try {
            Map<String, String> request = Map.of("text", text, "language", language);
            Map<String, String> response = restTemplate.postForObject(PYTHON_SERVICE_URL + "/tts", request, Map.class);
            if (response != null && response.containsKey("audio")) {
                return response.get("audio");
            }
        } catch (Exception e) {
            // Silently fail and fall back
        }
        return null;
    }

    private String generateWithOpenAi(String text, String language) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openAiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String instruction = buildInstruction(language);
            Map<String, Object> payload = Map.of(
                    "model", openAiTtsModel,
                    "voice", chooseVoice(language),
                    "format", "mp3",
                    "input", text,
                    "instructions", instruction
            );

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    "https://api.openai.com/v1/audio/speech",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    byte[].class
            );

            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                return null;
            }

            return Base64.getEncoder().encodeToString(body);
        } catch (Exception ex) {
            return null;
        }
    }

    private String chooseVoice(String language) {
        return switch (language == null ? "en" : language) {
            case "ta", "te", "kn", "ml", "hi", "bn", "gu", "mr", "pa", "or", "ur" -> "nova";
            case "fr", "de", "es", "ru" -> "alloy";
            case "ja", "zh", "ko" -> "shimmer";
            case "ar" -> "sage";
            default -> defaultVoice;
        };
    }

    private String buildInstruction(String language) {
        String target = switch (language == null ? "en" : language) {
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
        return "Speak clearly in " + target + ". Keep train names and station names natural. Read numbers naturally for speech.";
    }
}
