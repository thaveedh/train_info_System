package com.trainchatbot.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LanguageService {

    /**
     * Detect language using Unicode script ranges AND Romanized keyword hints.
     * Supports 20+ languages including Tanglish (Tamil in English script),
     * Hinglish (Hindi in English script), and other code-mixed varieties.
     */
    public String detectLanguage(String input) {
        if (input == null || input.isBlank()) {
            return "en";
        }

        // ── 1. Check Unicode script ranges for native scripts ──
        if (containsRange(input, 0x0B80, 0x0BFF)) return "ta";   // Tamil script
        if (containsRange(input, 0x0900, 0x097F)) return "hi";   // Devanagari (Hindi/Marathi)
        if (containsRange(input, 0x0C00, 0x0C7F)) return "te";   // Telugu
        if (containsRange(input, 0x0C80, 0x0CFF)) return "kn";   // Kannada
        if (containsRange(input, 0x0D00, 0x0D7F)) return "ml";   // Malayalam
        if (containsRange(input, 0x0980, 0x09FF)) return "bn";   // Bengali
        if (containsRange(input, 0x0A80, 0x0AFF)) return "gu";   // Gujarati
        if (containsRange(input, 0x0A00, 0x0A7F)) return "pa";   // Punjabi (Gurmukhi)
        if (containsRange(input, 0x0B00, 0x0B7F)) return "or";   // Odia
        if (containsRange(input, 0x0600, 0x06FF)) return "ur";   // Urdu/Arabic script
        if (containsRange(input, 0x3040, 0x30FF)) return "ja";   // Japanese
        if (containsRange(input, 0x4E00, 0x9FFF)) return "zh";   // Chinese
        if (containsRange(input, 0xAC00, 0xD7AF)) return "ko";   // Korean
        if (containsRange(input, 0x0400, 0x04FF)) return "ru";   // Cyrillic (Russian)

        // ── 2. Detect Tanglish (Tamil in English / Romanized Tamil) ──
        String normalized = input.toLowerCase(Locale.ROOT);

        // Tanglish keywords: extensive list covering common Tamil words in English
        if (containsAny(normalized,
                "enga", "engey", "irukku", "iruku", "eppo", "train eppo", "varum",
                "rayil", "station", "poguthu", "pogudhu", "poyirukku", "varugirathu",
                "ethanai neram", "vanthurukku", "varuthu", "vandhuruchu", "vandhuchu",
                "enna train", "entha train", "time aagum", "delay aagum",
                "inniku", "innaikki", "naalaikki", "naal", "porom",
                "vaanga", "thambi", "anna", "paa", "yeppa", "sollunga",
                "sollu", "konjam", "paaru", "parunga", "nokkuva",
                "seri", "pannunga", "pannalam", "evlo", "neram",
                "late ah", "late aa", "vanthachaa", "reach aagum",
                "poi irukku", "ennoda train", "ennaku", "enakku",
                "vandi", "erode", "kovai", "thanjavur", "salem",
                "madurai", "trichy", "tirunelveli", "rameswaram",
                "enga irukku", "enga irukkuthu", "enga iruku",
                "train enga", "vandi enga", "status sollu",
                "train kurippu", "train nilamai", "ponadhaa", "pochu",
                "varugiradhu", "vaara", "vaaraanga", "poittaa", "poittaanga"
        )) {
            return "ta";
        }

        // ── 3. Detect Hinglish (Hindi in English / Romanized Hindi) ──
        if (containsAny(normalized,
                "kahan", "kaha hai", "kab aayegi", "kitna late",
                "kitna time", "train kahan", "train kab", "gaadi kahan",
                "gaadi kab", "pahunchegi", "pahunch gayi", "station pe",
                "kaunsa station", "agla station", "delay hai", "late hai",
                "kitne minute", "meri train", "humari train", "batao",
                "bataiye", "bhai", "mujhe", "mujhko", "abhi kahan",
                "kidhar", "rukega", "rukegi", "aayega", "jaayega",
                "chalegi", "chal rahi", "rok rahi", "pehle",
                "baad mein", "time batao", "kab tak", "kitna aur",
                "aaj", "kal", "parso", "kya haal", "kaisa", "kaisi",
                "aap", "tum", "yahan", "wahan", "udhar", "idhar",
                "gaadi ko", "train ko", "nahi aaya", "nahi aayi",
                "delay kitna", "kab chalegi", "kab pahunchegi"
        )) {
            return "hi";
        }

        // ── 4. Detect Kanglish (Kannada in English) ──
        if (containsAny(normalized,
                "yelli", "elli idhey", "hogidhe", "bartide",
                "train yelli", "yavaaga", "nildana", "bandide",
                "station alli", "hogi iddhe", "hogi idhey",
                "train baratte", "baralla", "yeshtu late"
        )) {
            return "kn";
        }

        // ── 5. Detect Tenglish (Telugu in English) ──
        if (containsAny(normalized,
                "ekkada", "ekkada undhi", "eppudu", "eppudu vastadhi",
                "train ekkada", "ela undhi", "station lo",
                "vacchindhi", "raledhu", "delay ayyindhi",
                "entha time", "train ela", "vokati", "cheppandi"
        )) {
            return "te";
        }

        // ── 6. Detect Manglish (Malayalam in English) ──
        if (containsAny(normalized,
                "evide", "evide aanu", "eppol", "varum",
                "train evide", "ethra neram", "station il",
                "vanno", "ethiyilla", "late aano"
        )) {
            return "ml";
        }

        return "en";
    }

    public String extractTrainNumber(String input) {
        if (input == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(\\d{5})\\b").matcher(input);
        return m.find() ? m.group(1) : null;
    }

    private boolean containsRange(String text, int start, int end) {
        return text.codePoints().anyMatch(cp -> cp >= start && cp <= end);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
