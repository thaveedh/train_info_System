package com.trainchatbot.util;

import org.springframework.stereotype.Component;

@Component
public class SpeechTextUtil {

    private static final java.util.Map<String, String[]> DIGIT_WORDS = java.util.Map.ofEntries(
            java.util.Map.entry("en", new String[]{"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"}),
            java.util.Map.entry("ta", new String[]{"பூஜ்யம்", "ஒன்று", "இரண்டு", "மூன்று", "நான்கு", "ஐந்து", "ஆறு", "ஏழு", "எட்டு", "ஒன்பது"}),
            java.util.Map.entry("hi", new String[]{"शून्य", "एक", "दो", "तीन", "चार", "पांच", "छह", "सात", "आठ", "नौ"}),
            java.util.Map.entry("te", new String[]{"సున్నా", "ఒకటి", "రెండు", "మూడు", "నాలుగు", "ఐదు", "ఆరు", "ఏడు", "ఎనిమిది", "తొమ్మిది"}),
            java.util.Map.entry("kn", new String[]{"ಸೊನ್ನೆ", "ಒಂದು", "ಎರಡು", "ಮೂರು", "ನಾಲ್ಕು", "ಐದು", "ಆರು", "ಏಳು", "ಎಂಟು", "ಒಂಬತ್ತು"}),
            java.util.Map.entry("ml", new String[]{"പൂജ്യം", "ഒന്ന്", "രണ്ട്", "മൂന്ന്", "നാല്", "അഞ്ച്", "ആറ്", "ഏഴ്", "എട്ട്", "ഒമ്പത്"}),
            java.util.Map.entry("bn", new String[]{"শূন্য", "এক", "দুই", "তিন", "চার", "পাঁচ", "ছয়", "সাত", "আট", "নয়"}),
            java.util.Map.entry("gu", new String[]{"શૂન્ય", "એક", "બે", "ત્રણ", "ચાર", "પાંચ", "છ", "સાત", "આઠ", "નવ"}),
            java.util.Map.entry("mr", new String[]{"शून्य", "एक", "दोन", "तीन", "चार", "पाच", "सहा", "सात", "आठ", "नऊ"}),
            java.util.Map.entry("pa", new String[]{"ਸਿਫਰ", "ਇੱਕ", "ਦੋ", "ਤਿੰਨ", "ਚਾਰ", "ਪੰਜ", "ਛੇ", "ਸੱਤ", "ਅੱਠ", "ਨੌਂ"}),
            java.util.Map.entry("or", new String[]{"ଶୂନ", "ଏକ", "ଦୁଇ", "ତିନି", "ଚାରି", "ପାଞ୍ଚ", "ଛଅ", "ସାତ", "ଆଠ", "ନଅ"}),
            java.util.Map.entry("ur", new String[]{"صفر", "ایک", "دو", "تین", "چار", "پانچ", "چھ", "سات", "آٹھ", "نو"}),
            java.util.Map.entry("fr", new String[]{"zero", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf"}),
            java.util.Map.entry("de", new String[]{"null", "eins", "zwei", "drei", "vier", "funf", "sechs", "sieben", "acht", "neun"}),
            java.util.Map.entry("es", new String[]{"cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"}),
            java.util.Map.entry("ja", new String[]{"zero", "ichi", "ni", "san", "yon", "go", "roku", "nana", "hachi", "kyu"}),
            java.util.Map.entry("zh", new String[]{"ling", "yi", "er", "san", "si", "wu", "liu", "qi", "ba", "jiu"}),
            java.util.Map.entry("ko", new String[]{"yeong", "il", "i", "sam", "sa", "o", "yuk", "chil", "pal", "gu"}),
            java.util.Map.entry("ar", new String[]{"sifr", "wahid", "ithnan", "thalatha", "arbaa", "khamsa", "sitta", "sabaa", "thamaniya", "tisaa"}),
            java.util.Map.entry("ru", new String[]{"nol", "odin", "dva", "tri", "chetyre", "pyat", "shest", "sem", "vosem", "devyat"})
    );

    private static final String[] EN_ONES = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
    };

    private static final String[] EN_TENS = {
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    };

    private static final String[] TA_ONES = {
            "பூஜ்யம்", "ஒன்று", "இரண்டு", "மூன்று", "நான்கு",
            "ஐந்து", "ஆறு", "ஏழு", "எட்டு", "ஒன்பது"
    };

    public String speakCount(int number, String language) {
        if ("ta".equalsIgnoreCase(language)) {
            return tamilNumber(number);
        }
        if (!"en".equalsIgnoreCase(language)) {
            return speakDigits(String.valueOf(number), language);
        }
        return englishNumber(number);
    }

    public String speakDigits(String digits, String language) {
        if (digits == null || digits.isBlank()) {
            return "";
        }
        String[] words = DIGIT_WORDS.getOrDefault(language == null ? "en" : language.toLowerCase(), DIGIT_WORDS.get("en"));
        StringBuilder spoken = new StringBuilder();
        for (char ch : digits.toCharArray()) {
            if (!Character.isDigit(ch)) {
                continue;
            }
            if (spoken.length() > 0) {
                spoken.append(' ');
            }
            int digit = ch - '0';
            spoken.append(words[digit]);
        }
        return spoken.toString();
    }

    private String englishNumber(int number) {
        if (number < 0) {
            return "minus " + englishNumber(-number);
        }
        if (number < 20) {
            return EN_ONES[number];
        }
        if (number < 100) {
            int tens = number / 10;
            int remainder = number % 10;
            return remainder == 0 ? EN_TENS[tens] : EN_TENS[tens] + " " + EN_ONES[remainder];
        }
        if (number < 1000) {
            int hundreds = number / 100;
            int remainder = number % 100;
            String prefix = EN_ONES[hundreds] + " hundred";
            return remainder == 0 ? prefix : prefix + " " + englishNumber(remainder);
        }
        if (number < 100000) {
            int thousands = number / 1000;
            int remainder = number % 1000;
            String prefix = englishNumber(thousands) + " thousand";
            return remainder == 0 ? prefix : prefix + " " + englishNumber(remainder);
        }
        int lakhs = number / 100000;
        int remainder = number % 100000;
        String prefix = englishNumber(lakhs) + " lakh";
        return remainder == 0 ? prefix : prefix + " " + englishNumber(remainder);
    }

    private String tamilNumber(int number) {
        if (number < 0) {
            return "கழித்து " + tamilNumber(-number);
        }
        if (number < 10) {
            return TA_ONES[number];
        }
        return switch (number) {
            case 10 -> "பத்து";
            case 11 -> "பதினொன்று";
            case 12 -> "பன்னிரண்டு";
            case 13 -> "பதிமூன்று";
            case 14 -> "பதினான்கு";
            case 15 -> "பதினைந்து";
            case 16 -> "பதினாறு";
            case 17 -> "பதினேழு";
            case 18 -> "பதினெட்டு";
            case 19 -> "பத்தொன்பது";
            default -> tamilTensAndAbove(number);
        };
    }

    private String tamilTensAndAbove(int number) {
        if (number < 100) {
            String[] tensWords = {
                    "", "", "இருபது", "முப்பது", "நாற்பது", "ஐம்பது",
                    "அறுபது", "எழுபது", "எண்பது", "தொண்ணூறு"
            };
            int tens = number / 10;
            int remainder = number % 10;
            return remainder == 0 ? tensWords[tens] : tensWords[tens] + " " + TA_ONES[remainder];
        }
        if (number < 1000) {
            int hundreds = number / 100;
            int remainder = number % 100;
            String prefix = hundreds == 1 ? "நூறு" : TA_ONES[hundreds] + " நூறு";
            return remainder == 0 ? prefix : prefix + " " + tamilNumber(remainder);
        }
        return speakDigits(String.valueOf(number), "ta");
    }
}
