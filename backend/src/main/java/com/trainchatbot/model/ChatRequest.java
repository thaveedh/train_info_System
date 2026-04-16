package com.trainchatbot.model;

/**
 * ChatRequest
 *
 * UI input contract.
 * message: text input from user
 * voiceInputBase64: optional encoded voice content
 * voiceMode: whether voice features should be used
 */
public class ChatRequest {
    private String message;
    private String voiceInputBase64;
    private boolean voiceMode;
    private String targetLanguage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVoiceInputBase64() {
        return voiceInputBase64;
    }

    public void setVoiceInputBase64(String voiceInputBase64) {
        this.voiceInputBase64 = voiceInputBase64;
    }

    public boolean isVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(boolean voiceMode) {
        this.voiceMode = voiceMode;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
