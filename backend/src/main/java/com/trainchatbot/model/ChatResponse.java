package com.trainchatbot.model;

/**
 * ChatResponse
 *
 * API output contract returned to UI.
 */
public class ChatResponse {
    private String responseText;
    private String language;
    private String intent;
    private String trainNumber;
    private String audioBase64;
    private String nlSummary;
    private boolean fallbackUsed;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getNlSummary() {
        return nlSummary;
    }

    public void setNlSummary(String nlSummary) {
        this.nlSummary = nlSummary;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }
}
