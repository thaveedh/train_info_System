package com.trainchatbot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeechTextUtilTest {

    private final SpeechTextUtil speechTextUtil = new SpeechTextUtil();

    @Test
    void convertsEnglishCountsToWords() {
        assertEquals("fifteen", speechTextUtil.speakCount(15, "en"));
        assertEquals("one hundred twenty three", speechTextUtil.speakCount(123, "en"));
    }

    @Test
    void convertsTamilCountsToWords() {
        assertEquals("பதினைந்து", speechTextUtil.speakCount(15, "ta"));
        assertEquals("நூறு இருபது மூன்று", speechTextUtil.speakCount(123, "ta"));
    }

    @Test
    void spellsTrainDigitsForSpeech() {
        assertEquals("one two six two seven", speechTextUtil.speakDigits("12627", "en"));
        assertEquals("ஒன்று இரண்டு ஆறு இரண்டு ஏழு", speechTextUtil.speakDigits("12627", "ta"));
    }

    @Test
    void spellsDigitsForAdditionalLanguages() {
        assertEquals("एक दो छह दो सात", speechTextUtil.speakDigits("12627", "hi"));
        assertEquals("ఒకటి రెండు ఆరు రెండు ఏడు", speechTextUtil.speakDigits("12627", "te"));
    }
}
