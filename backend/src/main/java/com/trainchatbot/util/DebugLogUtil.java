package com.trainchatbot.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * DebugLogUtil
 *
 * Writes NDJSON logs for runtime evidence in debug mode.
 */
@Component
public class DebugLogUtil {
    private static final Path LOG_PATH = Paths.get("d:\\Multilingual_train\\debug-d0852f.log");

    public void log(String runId, String hypothesisId, String location, String message, Map<String, Object> data) {
        String payload = "{\"sessionId\":\"d0852f\",\"runId\":\"" + esc(runId) + "\",\"hypothesisId\":\"" + esc(hypothesisId)
                + "\",\"location\":\"" + esc(location) + "\",\"message\":\"" + esc(message)
                + "\",\"data\":" + toJsonObject(data) + ",\"timestamp\":" + System.currentTimeMillis() + "}";
        try {
            Files.write(LOG_PATH, (payload + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Debug logging must never break main flow.
        }
    }

    private String toJsonObject(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(esc(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(esc(String.valueOf(value))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String esc(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
