package com.trainchatbot.service;

import com.trainchatbot.model.ChatRequest;
import com.trainchatbot.model.ChatResponse;
import com.trainchatbot.model.IntentType;
import com.trainchatbot.model.LiveTrainTrackingJsonResponse;
import com.trainchatbot.model.TrainStatusResponse;
import com.trainchatbot.util.DebugLogUtil;
import com.trainchatbot.util.JsonParserUtil;
import com.trainchatbot.util.SpeechTextUtil;
import com.trainchatbot.util.VoiceUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ChatService
 *
 * Intent flow orchestrator:
 * UI input -> language detect -> entity extraction -> Gemini intent -> API Hub -> dynamic response -> translation.
 *
 * KEY BEHAVIOUR: The bot detects the user's language automatically and replies
 * in that same language. A natural language summary (nlSummary) is generated
 * by Gemini so that the user sees a human-friendly answer in their language,
 * alongside the structured train card.
 */
@Service
public class ChatService {

    private final LanguageService languageService;
    private final GeminiService geminiService;
    private final NlpService nlpService;
    private final TrainService trainService;
    private final LiveTrainRouteSimulationService liveTrainRouteSimulationService;
    private final JsonParserUtil jsonParserUtil;
    private final VoiceUtil voiceUtil;
    private final DebugLogUtil debugLogUtil;
    private final SpeechTextUtil speechTextUtil;

    public ChatService(LanguageService languageService,
                       GeminiService geminiService,
                       NlpService nlpService,
                       TrainService trainService,
                       LiveTrainRouteSimulationService liveTrainRouteSimulationService,
                       JsonParserUtil jsonParserUtil,
                       VoiceUtil voiceUtil,
                       DebugLogUtil debugLogUtil,
                       SpeechTextUtil speechTextUtil) {
        this.languageService = languageService;
        this.geminiService = geminiService;
        this.nlpService = nlpService;
        this.trainService = trainService;
        this.liveTrainRouteSimulationService = liveTrainRouteSimulationService;
        this.jsonParserUtil = jsonParserUtil;
        this.voiceUtil = voiceUtil;
        this.debugLogUtil = debugLogUtil;
        this.speechTextUtil = speechTextUtil;
    }

    public ChatResponse processChat(ChatRequest request) {
        String runId = "run-" + System.currentTimeMillis();

        String normalizedInput = request.getMessage();
        if ((normalizedInput == null || normalizedInput.isBlank()) && request.getVoiceInputBase64() != null) {
            normalizedInput = voiceUtil.voiceToText(request.getVoiceInputBase64());
        }
        if (normalizedInput == null) {
            normalizedInput = "";
        }

        // #region agent log
        debugLogUtil.log(runId, "H1", "ChatService.java:52", "Incoming user input", Map.of(
                "voiceMode", request.isVoiceMode(),
                "inputLength", normalizedInput.length()
        ));
        // #endregion

        /* ── STEP 1: Process User Message via Python NLP Brain ── */
        Map<String, Object> analysis = nlpService.analyzeMessage(normalizedInput);
        String language = (String) analysis.getOrDefault("language", "en");
        String trainNumber = (String) analysis.get("trainNumber");
        String intentStr = (String) analysis.getOrDefault("intent", "UNKNOWN");

        // #region agent log
        debugLogUtil.log(runId, "H2", "ChatService.java:64", "NLP Brain Analysis Result", Map.of(
                "language", language,
                "trainNumber", trainNumber == null ? "" : trainNumber,
                "intent", intentStr
        ));
        // #endregion

        ChatResponse response = new ChatResponse();
        response.setLanguage(language);
        response.setTrainNumber(trainNumber);
        response.setIntent(intentStr);

        /* ── No train number found → ask user in their language (via NLP Brain) ── */
        if (trainNumber == null) {
            String nlSummary = nlpService.generateFinalResponse(
                "GREETING or GENERAL_QUERY: No train number found. " + normalizedInput, language
            );
            if (nlSummary == null || nlSummary.isBlank()) {
                nlSummary = localizeNoTrainMessage(language);
            }

            response.setResponseText(nlSummary);
            response.setNlSummary(nlSummary);
            response.setAudioBase64(voiceUtil.textToVoiceBase64(nlSummary, language));
            return response;
        }

        /* ── STEP 2: Fetch Train Data ── */
        TrainStatusResponse trainData = trainService.fetchTrainData(trainNumber);
        
        /* ── Train not found ── */
        if (trainData.getTrainName() == null || trainData.getTrainName().isBlank()) {
            response.setFallbackUsed(trainData.isFallback());
            String nlSummary = nlpService.generateFinalResponse(
                "ERROR: Train number " + trainNumber + " not found. " + normalizedInput, language
            );
            if (nlSummary == null || nlSummary.isBlank()) {
                nlSummary = localizeNotFoundMessage(language, trainNumber);
            }

            response.setResponseText(nlSummary);
            response.setNlSummary(nlSummary);
            response.setAudioBase64(voiceUtil.textToVoiceBase64(nlSummary, language));
            return response;
        }

        /* ── STEP 3: Build live tracking simulation ── */
        LiveTrainTrackingJsonResponse payload =
                liveTrainRouteSimulationService.simulate(trainNumber, trainData, language, runId);
        String finalResponse = jsonParserUtil.toJson(payload);
        response.setResponseText(finalResponse);
        response.setFallbackUsed(trainData.isFallback());

        /* ── STEP 4: Generate natural language response via NLP Brain ── */
        String dataSummaryForAI = String.format(
                "Train: %s (%s). From %s to %s. Status: %s. Next: %s at %s. Delay: %d min. Progress: %d%%.",
                payload.getTrainName(), payload.getTrainNumber(),
                payload.getStations() != null && !payload.getStations().isEmpty() ? payload.getStations().get(0).getStationName() : "Unknown",
                payload.getStations() != null && !payload.getStations().isEmpty() ? payload.getStations().get(payload.getStations().size() - 1).getStationName() : "Unknown",
                payload.getCurrentStatus(), payload.getNextStation(), payload.getExpectedArrivalNext(),
                payload.getOverallDelayMinutes(), payload.getProgressPercent()
        );

        String nlSummary = nlpService.generateFinalResponse(dataSummaryForAI + "\nUser Question: " + normalizedInput, language);
        
        if (nlSummary == null || nlSummary.isBlank()) {
            nlSummary = buildVoiceSummary(payload, language);
        }

        response.setNlSummary(nlSummary);
        response.setAudioBase64(voiceUtil.textToVoiceBase64(nlSummary, language));

        // #region agent log
        debugLogUtil.log(runId, "H5", "ChatService.java:127", "Finalized response from AI Brain", Map.of(
                "language", language,
                "nlSummary", nlSummary
        ));
        // #endregion

        return response;
    }

    /**
     * Live train details as strict JSON (same contract as chat).
     */
    public LiveTrainTrackingJsonResponse fetchLiveTrainDetails(String trainNumber) {
        String runId = "run-" + System.currentTimeMillis();
        TrainStatusResponse trainData = trainService.fetchTrainData(trainNumber);
        if (trainData.getTrainName() == null || trainData.getTrainName().isBlank()) {
            return null;
        }
        return liveTrainRouteSimulationService.simulate(trainNumber, trainData, "en", runId);
    }

    private String buildVoiceSummary(LiveTrainTrackingJsonResponse p, String language) {
        return switch (language) {
            case "ta" -> String.format(
                    "ரயில் எண் %s, %s. %s. அடுத்த நிலையம் %s. வருகை %s. தாமதம் %s நிமிடங்கள்.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
            case "hi" -> String.format(
                    "ट्रेन नंबर %s, %s. %s. अगला स्टेशन %s. आगमन %s. देरी %s मिनट।",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
            case "te" -> String.format(
                    "రైలు %s, %s. %s. తదుపరి స్టేషన్ %s. రాక %s. ఆలస్యం %s నిమిషాలు.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
            case "kn" -> String.format(
                    "ರೈಲು %s, %s. %s. ಮುಂದಿನ ನಿಲ್ದಾಣ %s. ಆಗಮನ %s. ತಡ %s ನಿಮಿಷಗಳು.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
            case "ml" -> String.format(
                    "ട്രെയിൻ %s, %s. %s. അടുത്ത സ്റ്റേഷൻ %s. വരവ് %s. താമസം %s മിനിറ്റ്.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
            case "bn" -> String.format(
                    "ট্রেন %s, %s. %s. পরবর্তী স্টেশন %s. আগমন %s. বিলম্ব %s মিনিট.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
        default -> String.format(
                    "Train number %s, %s. %s. Next station %s. Expected arrival %s. Delay %s minutes.",
                    speechTextUtil.speakDigits(p.getTrainNumber(), language), p.getTrainName(), p.getCurrentStatus(), p.getNextStation(),
                    p.getExpectedArrivalNext(), speechTextUtil.speakCount(p.getOverallDelayMinutes(), language));
        };
    }

    private String localizeNoTrainMessage(String lang) {
        return switch (lang) {
            case "ta" -> "வணக்கம். ஐந்து இலக்க ரயில் எண்ணை சொல்லுங்கள். உதாரணமாக ஒன்று இரண்டு ஆறு இரண்டு ஏழு.";
            case "hi" -> "नमस्ते। कृपया पांच अंकों का ट्रेन नंबर बताइए, जैसे एक दो छह दो सात।";
            case "te" -> "నమస్కారం. ఐదు అంకెల ట్రైన్ నంబర్ చెప్పండి. ఉదాహరణకు ఒకటి రెండు ఆరు రెండు ఏడు.";
            case "kn" -> "ನಮಸ್ಕಾರ. ಐದು ಅಂಕಿಯ ರೈಲು ಸಂಖ್ಯೆಯನ್ನು ಹೇಳಿ. ಉದಾಹರಣೆಗೆ ಒಂದು ಎರಡು ಆರು ಎರಡು ಏಳು.";
            case "ml" -> "നമസ്കാരം. അഞ്ച് അക്ക ട്രെയിൻ നമ്പർ പറയൂ. ഉദാഹരണത്തിന് ഒന്ന് രണ്ട് ആറ് രണ്ട് ഏഴ്.";
            case "bn" -> "নমস্কার। পাঁচ অঙ্কের ট্রেন নম্বর বলুন। উদাহরণ এক দুই ছয় দুই সাত।";
            case "gu" -> "નમસ્તે. પાંચ અંકનો ટ્રેન નંબર કહો. ઉદાહરણ તરીકે એક બે છ બે સાત.";
            case "mr" -> "नमस्कार. पाच अंकी ट्रेन क्रमांक सांगा. उदाहरण एक दोन सहा दोन सात.";
            case "pa" -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ। ਪੰਜ ਅੰਕਾਂ ਦਾ ਟ੍ਰੇਨ ਨੰਬਰ ਦੱਸੋ। ਉਦਾਹਰਨ ਇੱਕ ਦੋ ਛੇ ਦੋ ਸੱਤ।";
            case "or" -> "ନମସ୍କାର। ପାଞ୍ଚ ଅଙ୍କର ଟ୍ରେନ ନମ୍ବର କହନ୍ତୁ। ଉଦାହରଣ ଏକ ଦୁଇ ଛଅ ଦୁଇ ସାତ।";
            case "ur" -> "سلام۔ پانچ ہندسوں کا ٹرین نمبر بتائیں۔ مثال ایک دو چھ دو سات۔";
            case "fr" -> "Bonjour. Please provide a five digit train number, for example one two six two seven.";
            case "de" -> "Hallo. Bitte nennen Sie eine funfstellige Zugnummer, zum Beispiel eins zwei sechs zwei sieben.";
            case "es" -> "Hola. Diga un numero de tren de cinco digitos, por ejemplo uno dos seis dos siete.";
            case "ja" -> "こんにちは。五桁の列車番号を教えてください。例えば いち に ろく に なな。";
            case "zh" -> "你好。请说出五位列车号码。例如 一 二 六 二 七。";
            case "ko" -> "안녕하세요. 다섯 자리 열차 번호를 말씀해 주세요. 예를 들면 일 이 육 이 칠.";
            case "ar" -> "مرحبا. قل رقم القطار المكون من خمسة ارقام. مثلا واحد اثنان ستة اثنان سبعة.";
            case "ru" -> "Здравствуйте. Назовите пятизначный номер поезда, например один два шесть два семь.";
            default -> "Hello. Please provide a valid five digit train number, for example one two six two seven.";
        };
    }

    private String localizeNotFoundMessage(String lang, String trainNo) {
        String spokenTrainNo = speechTextUtil.speakDigits(trainNo, lang);
        return switch (lang) {
            case "ta" -> "ரயில் எண் " + spokenTrainNo + " பற்றிய தகவல் கிடைக்கவில்லை. எண்ணை சரிபார்த்து மீண்டும் சொல்லுங்கள்.";
            case "hi" -> "ट्रेन नंबर " + spokenTrainNo + " की जानकारी नहीं मिली। कृपया नंबर जांचें।";
            case "te" -> "ట్రైన్ నంబర్ " + spokenTrainNo + " వివరాలు దొరకలేదు. నంబర్ తనిఖీ చేయండి.";
            case "kn" -> "ರೈಲು ಸಂಖ್ಯೆ " + spokenTrainNo + " ಮಾಹಿತಿ ಸಿಗಲಿಲ್ಲ. ಸಂಖ್ಯೆಯನ್ನು ಪರಿಶೀಲಿಸಿ.";
            case "ml" -> "ട്രെയിൻ നമ്പർ " + spokenTrainNo + " വിവരങ്ങൾ ലഭിച്ചില്ല. നമ്പർ പരിശോധിക്കുക.";
            case "bn" -> "ট্রেন নম্বর " + spokenTrainNo + " এর তথ্য পাওয়া যায়নি। নম্বর যাচাই করুন।";
            case "gu" -> "ટ્રેન નંબર " + spokenTrainNo + " ની માહિતી મળી નથી. નંબર ચકાસો.";
            case "mr" -> "ट्रेन क्रमांक " + spokenTrainNo + " ची माहिती मिळाली नाही. कृपया क्रमांक तपासा.";
            case "pa" -> "ਟ੍ਰੇਨ ਨੰਬਰ " + spokenTrainNo + " ਦੀ ਜਾਣਕਾਰੀ ਨਹੀਂ ਮਿਲੀ। ਨੰਬਰ ਜਾਂਚੋ।";
            case "or" -> "ଟ୍ରେନ ନମ୍ବର " + spokenTrainNo + " ର ସୂଚନା ମିଳିଲା ନାହିଁ। ନମ୍ବର ଯାଞ୍ଚ କରନ୍ତୁ।";
            case "ur" -> "ٹرین نمبر " + spokenTrainNo + " کی معلومات نہیں ملیں۔ براہ کرم نمبر چیک کریں۔";
            case "fr" -> "Could not find details for train number " + spokenTrainNo + ". Please verify the number.";
            case "de" -> "Details fur Zugnummer " + spokenTrainNo + " wurden nicht gefunden. Bitte prufen Sie die Nummer.";
            case "es" -> "No se encontraron detalles del tren numero " + spokenTrainNo + ". Verifique el numero.";
            case "ja" -> "列車番号 " + spokenTrainNo + " の情報が見つかりませんでした。番号を確認してください。";
            case "zh" -> "未找到列车号码 " + spokenTrainNo + " 的信息。请检查号码。";
            case "ko" -> "열차 번호 " + spokenTrainNo + " 정보를 찾을 수 없습니다. 번호를 확인해 주세요.";
            case "ar" -> "تعذر العثور على معلومات القطار رقم " + spokenTrainNo + ". يرجى التحقق من الرقم.";
            case "ru" -> "Не удалось найти данные по номеру поезда " + spokenTrainNo + ". Проверьте номер.";
            default -> "Could not find details for train number " + spokenTrainNo + ". Please verify the number.";
        };
    }
}
