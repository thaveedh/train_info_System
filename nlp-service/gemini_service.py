import json
import os
import re

import google.generativeai as genai
from dotenv import load_dotenv
from langdetect import detect, DetectorFactory

# Set seed for deterministic language detection
DetectorFactory.seed = 0

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY")
_HAS_REAL_KEY = GEMINI_API_KEY and GEMINI_API_KEY != "YOUR_GEMINI_API_KEY"

if _HAS_REAL_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    # Using gemini-1.5-flash as the most stable and available model
    for model_name in ["gemini-1.5-flash", "gemini-flash-latest"]:
        try:
            model = genai.GenerativeModel(model_name)
            model.generate_content("test")
            print(f"[OK] Gemini model '{model_name}' initialized successfully.")
            break
        except Exception as e:
            print(f"[ERROR] Model '{model_name}' not available: {e}. Trying fallback...")
            model = None
else:
    model = None
    print("[WARNING]: No Gemini API key found. Running in MOCK MODE.")


LANGUAGE_NAMES = {
    "en": "English",
    "hi": "Hindi",
    "ta": "Tamil",
    "te": "Telugu",
    "kn": "Kannada",
    "ml": "Malayalam",
    "bn": "Bengali",
    "gu": "Gujarati",
    "mr": "Marathi",
    "pa": "Punjabi",
    "or": "Odia",
    "ur": "Urdu",
    "fr": "French",
    "de": "German",
    "es": "Spanish",
    "ja": "Japanese",
    "zh": "Chinese",
    "ko": "Korean",
    "ar": "Arabic",
    "ru": "Russian",
}

TAMIL_BLOCK_RE = re.compile(r"[\u0B80-\u0BFF]")
DEVANAGARI_BLOCK_RE = re.compile(r"[\u0900-\u097F]")
TELUGU_BLOCK_RE = re.compile(r"[\u0C00-\u0C7F]")
KANNADA_BLOCK_RE = re.compile(r"[\u0C80-\u0CFF]")
MALAYALAM_BLOCK_RE = re.compile(r"[\u0D00-\u0D7F]")
BENGALI_BLOCK_RE = re.compile(r"[\u0980-\u09FF]")
GUJARATI_BLOCK_RE = re.compile(r"[\u0A80-\u0AFF]")
PUNJABI_BLOCK_RE = re.compile(r"[\u0A00-\u0A7F]")
ODIA_BLOCK_RE = re.compile(r"[\u0B00-\u0B7F]")
ARABIC_BLOCK_RE = re.compile(r"[\u0600-\u06FF]")
JAPANESE_BLOCK_RE = re.compile(r"[\u3040-\u30FF]")
CHINESE_BLOCK_RE = re.compile(r"[\u4E00-\u9FFF]")
KOREAN_BLOCK_RE = re.compile(r"[\uAC00-\uD7AF]")
CYRILLIC_BLOCK_RE = re.compile(r"[\u0400-\u04FF]")
LATIN_TOKEN_RE = re.compile(r"[a-z]+")
TRAIN_NUMBER_RE = re.compile(r"\b(\d{5})\b")
NOISE_RE = re.compile(r"[^\w\s\u0B80-\u0BFF]")
REPEATED_CHAR_RE = re.compile(r"(.)\1{2,}")
MULTISPACE_RE = re.compile(r"\s+")

TANGLISH_MAP = {
    "enga": "எங்க",
    "enge": "எங்க",
    "iruka": "இருக்க",
    "irukku": "இருக்கு",
    "iruku": "இருக்கு",
    "varuthu": "வருது",
    "varudhu": "வருது",
    "varum": "வரும்",
    "eppo": "எப்போ",
    "epo": "எப்போ",
    "enna": "என்ன",
    "entha": "எந்த",
    "nilai": "நிலை",
    "nilamai": "நிலைமை",
    "sollu": "சொல்லு",
    "sollunga": "சொல்லுங்க",
    "train": "ட்ரெயின்",
    "rayil": "ரயில்",
    "vandi": "வண்டி",
    "status": "நிலை",
    "late": "தாமதம்",
    "station": "ஸ்டேஷன்",
}

TAMIL_HINTS = {
    "enga", "enge", "iruka", "iruku", "irukku", "varuthu", "varudhu", "varum",
    "eppo", "epo", "enna", "entha", "rayil", "vandi", "sollu", "sollunga",
    "nilamai", "kovai", "madurai", "trichy", "salem", "thanjavur", "ennai",
    "enakku", "ennoda", "inga", "anga", "vanakkam",
}

ROMANIZED_HINTS = {
    "ta": TAMIL_HINTS,
    "hi": {"kahan", "kaha", "kab", "gaadi", "pahunch", "batao", "kidhar", "meri", "deri", "kitna", "late"},
    "te": {"ekkada", "eppudu", "vastadhi", "vacchindhi", "cheppandi", "raledhu"},
    "kn": {"yelli", "hogidhe", "bartide", "bandide", "yavaaga", "yeshtu"},
    "ml": {"evide", "eppol", "vanno", "ethra", "aano"},
}

GREETING_HINTS = {"hello", "hi", "hey", "vanakkam", "வணக்கம்"}

TRAIN_STATUS_HINTS = {
    "train", "ட்ரெயின்", "ரயில்", "where", "status", "delay", "late", "station",
    "arrival", "reach", "eta", "எங்க", "எப்போ", "நிலை", "வரும்", "வருது", "தாமதம்",
}

def _collapse_repeats(text):
    return REPEATED_CHAR_RE.sub(r"\1", text)

def _strip_noise(text):
    text = NOISE_RE.sub(" ", text)
    return MULTISPACE_RE.sub(" ", text).strip()

def normalize_text(text):
    text = text or ""
    text = text.strip()
    text = _collapse_repeats(text)
    return _strip_noise(text)

def normalize_tanglish(text):
    lowered = normalize_text(text).lower()
    tokens = [TANGLISH_MAP.get(token, token) for token in lowered.split()]
    return " ".join(tokens)

DETECTION_SYSTEM_PROMPT = """
Analyze the input text and return ONLY the 2-letter BCP-47 language code.
Identify Tanglish as 'ta', Hinglish as 'hi', Banglish as 'bn'.
Supported codes: en, hi, ta, te, kn, ml, bn, gu, mr, pa, or, ur, fr, de, es, ja, zh, ko, ar, ru.
Constraint: Return ONLY the code (e.g., 'ta'). No words, no punctuation.
"""

def detect_language(message):
    message = (message or "").strip()
    if not message:
        return "en"

    # 1. High-Speed Script Checks (100% accurate for unique scripts)
    if TAMIL_BLOCK_RE.search(message): return "ta"
    if DEVANAGARI_BLOCK_RE.search(message): return "hi"
    if TELUGU_BLOCK_RE.search(message): return "te"
    if KANNADA_BLOCK_RE.search(message): return "kn"
    if MALAYALAM_BLOCK_RE.search(message): return "ml"
    if BENGALI_BLOCK_RE.search(message): return "bn"
    if GUJARATI_BLOCK_RE.search(message): return "gu"
    if PUNJABI_BLOCK_RE.search(message): return "pa"
    if ODIA_BLOCK_RE.search(message): return "or"
    if ARABIC_BLOCK_RE.search(message): return "ur"
    if JAPANESE_BLOCK_RE.search(message): return "ja"
    if CHINESE_BLOCK_RE.search(message): return "zh"
    if KOREAN_BLOCK_RE.search(message): return "ko"
    if CYRILLIC_BLOCK_RE.search(message): return "ru"
    
    # 2. Heuristic Keyword Check for Romanized Indian Languages
    lowered = normalize_text(message).lower()
    tokens = set(LATIN_TOKEN_RE.findall(lowered))
    for lang, hints in ROMANIZED_HINTS.items():
        if tokens & hints:
            return lang

    # 3. AI-Powered Detection for Latin/Mixed script (English, French, Tanglish, etc.)
    ai_code = get_ai_detection(message)
    if ai_code:
        return ai_code

    # 4. Final Fallback: langdetect
    try:
        detected_code = detect(message)
        if detected_code in LANGUAGE_NAMES:
            return detected_code
    except:
        pass

    return "en"

def detect_intent(message):
    lowered = message.lower()
    if any(hint in lowered for hint in GREETING_HINTS):
        return "GREETING"
    if TRAIN_NUMBER_RE.search(message) or any(hint in lowered for hint in TRAIN_STATUS_HINTS):
        return "TRAIN_STATUS"
    return "GENERAL_QUERY"

def detect_language_and_intent(message):
    normalized = normalize_text(message)
    language = detect_language(normalized)
    processed = normalize_tanglish(normalized) if language == "ta" else normalized
    train_match = TRAIN_NUMBER_RE.search(normalized)
    return {
        "language": language,
        "normalizedText": processed,
        "intent": detect_intent(processed),
        "trainNumber": train_match.group(1) if train_match else None,
    }

SYSTEM_PROMPT = """
You are an advanced multilingual railway assistant AI.

CORE OBJECTIVE:
Automatically understand the user’s language, process train-related queries, and respond in the SAME language in a clean, speech-friendly format.

1. AUTO LANGUAGE DETECTION (MANDATORY)
- Detect the language of the input automatically.
- Supported languages include (but not limited to): English, Tamil, Hindi, Telugu, Kannada, Malayalam, Bengali, Gujarati, Marathi, Punjabi, Odia, Urdu, French, German, Spanish, Japanese, Chinese, Korean, Arabic, Russian.
- If input is Tanglish (Tamil written in English letters): Convert it into proper Tamil internally before processing.

2. NO MANUAL LANGUAGE SELECTION
- Ignore any passed language hints if they contradict your detection. Always rely ONLY on detected language.

3. INTENT UNDERSTANDING
- Identify user intent: TRAIN_STATUS, GREETING, or GENERAL_QUERY.

4. TRAIN INFORMATION PROCESSING
- Extract: train number, train name, source and destination.
- If data is missing: Ask a short follow-up question in the SAME language.

5. RESPONSE LANGUAGE RULE
- Always respond in the SAME language as the user input.
- If Tanglish input: Respond in PURE Tamil script.
- Do NOT mix languages.

6. NUMBER HANDLING (CRITICAL)
- NEVER output raw digits.
- Convert all numbers into spoken words in the detected language.
- For Train Numbers (5 digits), spell them out digit by digit. 

7. TEXT NORMALIZATION
- Clean noisy input. Convert informal phrases into meaningful sentences.

8. SPEECH (TTS) OPTIMIZATION
- Output must be: Natural, Short, Clear, Easy to pronounce.
- No symbols, no markdown, no JSON.

9. ERROR HANDLING
- If unclear: Ask a short clarification question. Never guess.

10. STRICT OUTPUT RULES
- Output ONLY the final response sentence.
- No explanations. No metadata. No JSON. No raw numbers. No broken text.
"""

def get_ai_response(prompt, target_language="en"):
    if not _HAS_REAL_KEY:
        return ""

    try:
        lang_name = LANGUAGE_NAMES.get(target_language, "English")
        lang_directive = f"YOU MUST RESPOND ONLY IN {lang_name}."
        if target_language == "ta":
            lang_directive += " USE PROPER TAMIL SCRIPT (தமிழ்)."
        
        full_prompt = f"{SYSTEM_PROMPT}\n\n{lang_directive}\n\nDATA TO PROCESS:\n{prompt}"
        
        response = model.generate_content(full_prompt)
        return response.text.strip()
    except Exception as e:
        error_str = str(e).lower()
        if "429" in error_str or "quota" in error_str or "exhausted" in error_str:
            print("[QUOTA EXCEEDED] Falling back to offline response generation.")
        else:
            print(f"Gemini API Error: {e}")
        return ""

def get_ai_detection(message):
    """
    Dedicated AI detection call with quota-aware fallback.
    """
    if not _HAS_REAL_KEY:
        return None
        
    try:
        detect_prompt = f"{DETECTION_SYSTEM_PROMPT}\n\nINPUT: {message}"
        response = model.generate_content(detect_prompt)
        code = response.text.strip().lower()[:2]
        if code in LANGUAGE_NAMES:
            return code
    except Exception as e:
        # Ignore detect errors and let heuristic fallbacks run
        pass
    return None

def digit_to_word(digit, lang):
    digit = str(digit)
    en_words = ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"]
    # Informal spoken Tamil digits for a more natural feel
    ta_words = ["சைபர்", "ஒண்ணு", "ரெண்டு", "மூணு", "நாலு", "அஞ்சு", "ஆறு", "ஏழு", "எட்டு", "ஒம்போது"]
    hi_words = ["शून्य", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ"]
    
    try:
        idx = int(digit)
        if lang == "ta": return ta_words[idx]
        if lang == "hi": return hi_words[idx]
        return en_words[idx]
    except:
        return digit

def spell_out_number(number_str, lang):
    words = [digit_to_word(d, lang) for d in number_str if d.isdigit()]
    return " ".join(words)

def replace_digits_with_words(text, target_language):
    def repl(match):
        num_str = match.group()
        return spell_out_number(num_str, target_language)

    return re.sub(r"\d+", repl, text)

def format_multilingual_response(data, target_language):
    prompt = f"Data Summary: {data}"
    output = get_ai_response(prompt, target_language)
    
    if not output:
        return _build_offline_response(data, target_language)
    
    output = replace_digits_with_words(output, target_language)
    output = re.sub(r'[*_#`]', '', output)
    return output

from gtts import gTTS
import io
import base64

def generate_tts_audio(text, lang="en"):
    """
    Generate high-quality audio using Google TTS (gTTS) and return as Base64.
    Acts as a superior 'alternate way' for Tamil speech.
    """
    try:
        # Map our internal lang codes to gTTS codes
        gtts_lang = lang if lang != "ur" else "ur" # gTTS supports 'ta', 'hi', 'en', etc.
        
        tts = gTTS(text=text, lang=gtts_lang, slow=False)
        fp = io.BytesIO()
        tts.write_to_fp(fp)
        fp.seek(0)
        
        audio_b64 = base64.b64encode(fp.read()).decode('utf-8')
        return audio_b64
    except Exception as e:
        print(f"gTTS Error: {e}")
        return None

def _build_offline_response(data, lang):
    """
    Build a high-quality, natural-sounding response even when AI is offline.
    Parses the data string and reconstructs a friendly summary.
    """
    try:
        # data is a string like "Train: Express 14338... From Madurai to Kolkata..."
        # We'll use regex to pull out what we need
        train_no = re.search(r'(\d{5})', data)
        train_no = train_no.group(1) if train_no else "—"
        
        fro = re.search(r'From (.*?) to', data)
        fro = fro.group(1) if fro else "—"
        
        to = re.search(r'to (.*?)\.', data)
        to = to.group(1) if to else "—"
        
        status = re.search(r'Status: (.*?)\.', data)
        status = status.group(1) if status else "—"
        
        next_st = re.search(r'Next: (.*?) at', data)
        next_st = next_st.group(1) if next_st else "—"

        # Natural templates
        if lang == "ta":
            return f"ரயில் எண் {spell_out_number(train_no, 'ta')}, {fro} முதல் {to} வரை. தற்போது {status}. அடுத்த நிலையம் {next_st}."
        
        if lang == "hi":
            return f"ट्रेन नंबर {spell_out_number(train_no, 'hi')}, {fro} से {to} तक। वर्तमान स्थिति: {status}. अगला स्टेशन: {next_st}."

        return f"Train {spell_out_number(train_no, 'en')}, from {fro} to {to}. Current status: {status}. Next station is {next_st}."
    except Exception as e:
        if lang == "ta": return "ரயில் விவரங்கள் இதோ."
        return "Here are the train details."
