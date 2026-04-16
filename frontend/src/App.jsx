import React, { useEffect, useRef, useState, useCallback } from 'react';
import { Bot, Clock, Globe, MapPin, Mic, MicOff, Send, Train, User, Volume2, VolumeX } from 'lucide-react';

/* ──────────────────────────────────────────────────────────────
 * LANGUAGE MAP — maps BCP-47 codes to display names & TTS codes
 * ────────────────────────────────────────────────────────────── */
const LANG_MAP = {
  en: { name: 'English', speechCode: 'en-IN', flag: '🇬🇧' },
  hi: { name: 'हिन्दी', speechCode: 'hi-IN', flag: '🇮🇳' },
  ta: { name: 'தமிழ்', speechCode: 'ta-IN', flag: '🇮🇳' },
  te: { name: 'తెలుగు', speechCode: 'te-IN', flag: '🇮🇳' },
  kn: { name: 'ಕನ್ನಡ', speechCode: 'kn-IN', flag: '🇮🇳' },
  ml: { name: 'മലയാളം', speechCode: 'ml-IN', flag: '🇮🇳' },
  mr: { name: 'मराठी', speechCode: 'mr-IN', flag: '🇮🇳' },
  bn: { name: 'বাংলা', speechCode: 'bn-IN', flag: '🇮🇳' },
  gu: { name: 'ગુજરાતી', speechCode: 'gu-IN', flag: '🇮🇳' },
  pa: { name: 'ਪੰਜਾਬੀ', speechCode: 'pa-IN', flag: '🇮🇳' },
  or: { name: 'ଓଡ଼ିଆ', speechCode: 'or-IN', flag: '🇮🇳' },
  ur: { name: 'اردو', speechCode: 'ur-IN', flag: '🇮🇳' },
  fr: { name: 'Français', speechCode: 'fr-FR', flag: '🇫🇷' },
  de: { name: 'Deutsch', speechCode: 'de-DE', flag: '🇩🇪' },
  es: { name: 'Español', speechCode: 'es-ES', flag: '🇪🇸' },
  ja: { name: '日本語', speechCode: 'ja-JP', flag: '🇯🇵' },
  zh: { name: '中文', speechCode: 'zh-CN', flag: '🇨🇳' },
  ko: { name: '한국어', speechCode: 'ko-KR', flag: '🇰🇷' },
  ar: { name: 'العربية', speechCode: 'ar-SA', flag: '🇸🇦' },
  ru: { name: 'Русский', speechCode: 'ru-RU', flag: '🇷🇺' },
};

const RECOGNITION_OPTIONS = Object.entries(LANG_MAP).map(([code, info]) => ({
  code,
  label: info.name,
  speechCode: info.speechCode
}));

const EN_DIGIT_WORDS = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine'];

const spellDigitsForSpeech = (value, langCode = 'en') => {
  const text = String(value ?? '');
  if (!/\d/.test(text)) {
    return text;
  }

  if (langCode === 'en') {
    return text.replace(/\d/g, (digit) => EN_DIGIT_WORDS[Number(digit)]);
  }

  return text.replace(/\d/g, (digit) => ` ${digit} `).replace(/\s+/g, ' ').trim();
};

/* ──────────────────────────────────────────────────────
 * Parse structured train JSON from backend response
 * ────────────────────────────────────────────────────── */
const parseTrainResponse = (text) => {
  if (!text) {
    return null;
  }

  const normalized = text
    .replace(/â|â€"/g, '-')
    .replace(/^Unable to generate AI response\. Train Details:\s*/, '')
    .trim();

  try {
    const parsed = JSON.parse(normalized);
    // Advanced live tracking JSON (full route + position)
    if (parsed?.train_number && parsed?.stations && Array.isArray(parsed.stations)) {
      const od = Number(parsed.overall_delay_minutes);
      const sts = parsed.stations;
      const first = sts[0];
      const last = sts[sts.length - 1];
      return {
        trainNo: parsed.train_number,
        trainName: parsed.train_name,
        from: first?.station_name || '—',
        to: last?.station_name || '—',
        departureTime: first?.departure || '—',
        currentStatus: parsed.current_status || '',
        currentLocation: parsed.current_station || '—',
        nextStation: parsed.next_station || '—',
        eta: parsed.expected_arrival_next || '—',
        delay: Number.isFinite(od) ? `${od} mins` : '0 mins',
        statusType:
          parsed.progress_percent >= 100 ? 'ARRIVED' : od > 0 ? 'DELAYED' : 'ON_TIME',
        progressPercent: parsed.progress_percent,
        distanceKm: parsed.distance_remaining_km,
        stationsList: sts
      };
    }
    // Legacy compact live JSON
    if (parsed?.train_number && parsed?.train_name) {
      const dm = Number(parsed.delay_minutes ?? parsed.overall_delay_minutes);
      return {
        trainNo: parsed.train_number,
        trainName: parsed.train_name,
        from: parsed.from || '—',
        to: parsed.to || '—',
        departureTime: parsed.departure_time || '—',
        currentStatus: parsed.status || parsed.current_status || '',
        currentLocation: parsed.next_station ? `Approaching ${parsed.next_station}` : (parsed.status || '—'),
        nextStation: parsed.next_station || '—',
        eta: parsed.estimated_arrival || parsed.expected_arrival_next,
        delay: Number.isFinite(dm) ? `${dm} mins` : '—',
        statusType: parsed.status_type || (dm > 0 ? 'DELAYED' : 'ON_TIME')
      };
    }
    if (parsed?.trainNo && parsed?.trainName) {
      return parsed;
    }
  } catch (error) {
    return null;
  }

  return null;
};

/* ────────────────────────────────────────────────────────────────
 * Build plain-text speech summary from a message
 * ──────────────────────────────────────────────────────────────── */
const buildSpeechText = (message) => {
  // If we have a natural language summary, prefer that for TTS
  if (message.nlSummary) {
    return message.nlSummary;
  }

  const train = parseTrainResponse(message.text);
  if (!train) {
    return message.text.replace(/\[MOCK MODE\]/g, '').replace(/Found in Schedule Dataset:/g, '');
  }

  const langCode = message.language || 'en';

  return [
    `Train ${spellDigitsForSpeech(train.trainNo, langCode)}, ${train.trainName}.`,
    `From ${train.from} to ${train.to}.`,
    `Current status: ${train.currentStatus}.`,
    `Next station: ${train.nextStation}.`,
    `Estimated arrival: ${train.eta}.`,
    `Delay: ${spellDigitsForSpeech(train.delay, langCode)}.`,
    train.progressPercent != null ? `Progress: ${spellDigitsForSpeech(train.progressPercent, langCode)} percent.` : ''
  ]
    .filter(Boolean)
    .join(' ');
};

/* ────────────────────────────────────────────────────
 * Detect a BCP-47 voice for a given language code
 * ──────────────────────────────────────────────────── */
const findVoiceForLang = (langCode) => {
  const voices = window.speechSynthesis.getVoices();
  if (voices.length === 0) return null;

  const langInfo = LANG_MAP[langCode] || LANG_MAP['en'];
  const targetCode = langInfo.speechCode.toLowerCase();
  const searchKey = langCode.toLowerCase();
  
  // 1. Strict match by language tag
  let candidates = voices.filter(v => v.lang.toLowerCase().replace('_', '-') === targetCode);
  
  // 2. Secondary match by script name in the voice name
  if (candidates.length === 0) {
    const scriptHint = searchKey === 'ta' ? 'tamil' : searchKey === 'hi' ? 'hindi' : null;
    if (scriptHint) {
      candidates = voices.filter(v => v.name.toLowerCase().includes(scriptHint));
    }
  }

  // 3. Prefix match
  if (candidates.length === 0) {
    candidates = voices.filter(v => v.lang.toLowerCase().startsWith(searchKey));
  }

  if (candidates.length === 0) {
    // If it's Tamil and we found NOTHING, it's better to return null 
    // so it doesn't try to read Tamil with an English voice.
    return searchKey === 'ta' ? null : (voices.find(v => v.default) || voices[0]);
  }

  // Choose Premium voices
  const premium = candidates.find(v => 
    v.name.toLowerCase().includes('google') || 
    v.name.toLowerCase().includes('natural') || 
    v.name.toLowerCase().includes('online') ||
    v.name.toLowerCase().includes('valluvar') ||
    v.name.toLowerCase().includes('pallavi')
  );

  return premium || candidates[0];
};

/* ─────────────────────────
 * Train Status Card
 * ───────────────────────── */
const TrainStatusCard = ({ train }) => (
  <div className="train-card live-card">
    <div className="card-header">
      <div className="card-title">
        <Train size={18} />
        <span className="train-num">{train.trainNo}</span>
        <span className="train-name">{train.trainName}</span>
      </div>
      <span className={`status-chip ${String(train.statusType || '').toLowerCase()}`}>
        {train.statusType}
      </span>
    </div>
    <div className="card-body">
      <div className="route route-compact">
        <div className="station">
          <MapPin size={14} />
          <span>{train.from}</span>
        </div>
        <div className="connector"></div>
        <div className="station station-end">
          <MapPin size={14} />
          <span>{train.to}</span>
        </div>
      </div>

      <div className="detail-grid">
        <div className="detail-item">
          <span className="detail-label">Departure</span>
          <span className="detail-value">{train.departureTime}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Delay</span>
          <span className="detail-value">{train.delay}</span>
        </div>
        <div className="detail-item detail-wide">
          <span className="detail-label">Current Status</span>
          <span className="detail-value">{String(train.currentStatus || '').replace(/â|â€"/g, '-')}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Current Location</span>
          <span className="detail-value">{train.currentLocation}</span>
        </div>
        <div className="detail-item">
          <span className="detail-label">Next Station</span>
          <span className="detail-value">{train.nextStation}</span>
        </div>
        <div className="detail-item detail-wide timing">
          <Clock size={14} />
          <span>ETA next {train.eta}</span>
        </div>
        {train.progressPercent != null && (
          <div className="detail-item detail-wide">
            <span className="detail-label">Route progress</span>
            <span className="detail-value">{train.progressPercent}%</span>
          </div>
        )}
        {train.distanceKm != null && (
          <div className="detail-item">
            <span className="detail-label">Km to next</span>
            <span className="detail-value">{train.distanceKm} km</span>
          </div>
        )}
      </div>
    </div>
  </div>
);

/* ──────────────────────────────────
 * Bot Message with NL summary + card
 * ────────────────────────────────── */
const BotMessage = ({ text, nlSummary, language }) => {
  const train = parseTrainResponse(text);
  const langInfo = LANG_MAP[language];

  return (
    <div className="bot-message-content">
      {/* Language badge */}
      {language && langInfo && (
        <div className="lang-badge">
          <Globe size={12} />
          <span>{langInfo.flag} {langInfo.name}</span>
        </div>
      )}

      {/* Natural language summary (the smart response) */}
      {nlSummary && (
        <div className="nl-summary">{nlSummary}</div>
      )}

      {/* Structured train card */}
      {train && <TrainStatusCard train={train} />}

      {/* Fallback plain text if no NL summary and no card */}
      {!nlSummary && !train && <div className="text-content">{text}</div>}
    </div>
  );
};

/* ──────────────────────────────────────
 * Voice Waveform Animation Component
 * ────────────────────────────────────── */
const VoiceWaveform = () => (
  <div className="voice-waveform">
    {[...Array(5)].map((_, i) => (
      <span key={i} className="wave-bar" style={{ animationDelay: `${i * 0.12}s` }}></span>
    ))}
  </div>
);

/* ═════════════════════════════════════
 * MAIN APP COMPONENT
 * ═════════════════════════════════════ */
const App = () => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      text: 'Hello! I am your Multilingual RailBot 🚄\n\nAsk me anything about trains — in any language! You can type or use the 🎤 microphone.\n\nExamples:\n• "Where is train 12627?"\n• "ट्रेन 12301 कहाँ है?"\n• "12633 ரயில் எங்கே?"',
      sender: 'bot'
    }
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [ttsEnabled, setTtsEnabled] = useState(true);
  const [recognitionLang, setRecognitionLang] = useState('en-IN'); // Default to Indian English for mixed text
  const [detectedVoiceLang, setDetectedVoiceLang] = useState(null);
  const messagesEndRef = useRef(null);
  const recognitionRef = useRef(null);
  const audioRef = useRef(null);

  /* ── Speech Recognition (multilingual) ── */
  const startListening = useCallback(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Voice search is not supported in this browser. Please use Chrome or Edge.');
      return;
    }

    // Stop any ongoing speech synthesis
    window.speechSynthesis.cancel();
    setIsSpeaking(false);

    const recognition = new SpeechRecognition();

    // Enable CONTINUOUS recognition with multiple language hints
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.maxAlternatives = 1;

    // Use selected language
    recognition.lang = recognitionLang;

    recognition.onstart = () => {
      setIsListening(true);
      setDetectedVoiceLang(null);
    };

    recognition.onresult = (event) => {
      const result = event.results[event.results.length - 1];
      const transcript = result[0].transcript;

      setInput(transcript);

      // If this is the FINAL result, auto-send it
      if (result.isFinal) {
        setIsListening(false);
        // Detect language from the transcript for the lang badge
        const langCode = detectScriptLanguage(transcript);
        setDetectedVoiceLang(langCode);

        // Auto-send after a brief delay to show the text in the input field
        setTimeout(() => {
          handleSendMessage(transcript);
        }, 300);
      }
    };

    recognition.onerror = (event) => {
      console.error('Speech recognition error:', event.error);
      setIsListening(false);
      if (event.error === 'not-allowed') {
        alert('Microphone access denied. Please allow microphone permissions.');
      }
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = recognition;
    try {
      recognition.start();
    } catch (e) {
      console.error("Recognition start error:", e);
      setIsListening(false);
    }
  }, [recognitionLang]); // eslint-disable-line react-hooks/exhaustive-deps

  const stopListening = useCallback(() => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
    }
    setIsListening(false);
  }, []);

  /* ── Detect language from script AND keywords (client-side hint) ── */
  const detectScriptLanguage = (text) => {
    // Unicode script detection
    if (/[\u0900-\u097F]/.test(text)) return 'hi';
    if (/[\u0B80-\u0BFF]/.test(text)) return 'ta';
    if (/[\u0C00-\u0C7F]/.test(text)) return 'te';
    if (/[\u0C80-\u0CFF]/.test(text)) return 'kn';
    if (/[\u0D00-\u0D7F]/.test(text)) return 'ml';
    if (/[\u0980-\u09FF]/.test(text)) return 'bn';
    if (/[\u0A80-\u0AFF]/.test(text)) return 'gu';
    if (/[\u0A00-\u0A7F]/.test(text)) return 'pa';
    if (/[\u0B00-\u0B7F]/.test(text)) return 'or';
    if (/[\u0600-\u06FF]/.test(text)) return 'ur';
    if (/[\u3040-\u309F\u30A0-\u30FF]/.test(text)) return 'ja';
    if (/[\u4E00-\u9FFF]/.test(text)) return 'zh';
    if (/[\uAC00-\uD7AF]/.test(text)) return 'ko';
    if (/[\u0400-\u04FF]/.test(text)) return 'ru';

    // Tanglish detection (Tamil in English script)
    const lower = text.toLowerCase();
    if (/\b(enga|irukku|iruku|eppo|varum|rayil|poguthu|vandi|sollu|paaru|ennoda|enakku|thambi|anna)\b/.test(lower)) return 'ta';
    // Hinglish detection (Hindi in English script)
    if (/\b(kahan|kaha hai|kab|gaadi|pahunch|batao|kidhar|meri train|abhi kahan|kitna late|chal rahi)\b/.test(lower)) return 'hi';
    // Kanglish detection
    if (/\b(yelli|hogidhe|bartide|bandide|baratte)\b/.test(lower)) return 'kn';
    // Tenglish detection
    if (/\b(ekkada|eppudu|vastadhi|vacchindhi)\b/.test(lower)) return 'te';
    // Manglish detection
    if (/\b(evide|eppol|vanno|ethra)\b/.test(lower)) return 'ml';

    return 'en';
  };

  /* ── Text-to-Speech with language matching ── */
  // Store utterances globally to prevent Chrome from garbage-collecting them mid-speech
  window.speechUtterances = window.speechUtterances || [];

  const speakResponse = useCallback((message, langCode, forceSpeak = false) => {
    if (!ttsEnabled && !forceSpeak) return;
    if (!message) return;

    const playBrowserSpeech = () => {
      const speechText = buildSpeechText(message);
      if (!speechText || speechText.trim().length === 0) return;
      if (!('speechSynthesis' in window)) {
        console.warn('Speech synthesis not supported in this browser.');
        return;
      }

      window.speechSynthesis.cancel();

      const utterance = new SpeechSynthesisUtterance(speechText);
      utterance.rate = 0.95;
      utterance.pitch = 1.0;

      const effectiveLang = langCode || 'en';
      const langInfo = LANG_MAP[effectiveLang];
      if (langInfo) {
        utterance.lang = langInfo.speechCode;
      }

      const voice = findVoiceForLang(effectiveLang);
      if (voice) {
        utterance.voice = voice;
      }

      utterance.onstart = () => {
        setIsSpeaking(true);
        console.log('Browser TTS playing:', speechText);
      };
      utterance.onend = () => {
        setIsSpeaking(false);
        window.speechUtterances = window.speechUtterances.filter(u => u !== utterance);
      };
      utterance.onerror = (e) => {
        console.error('Browser TTS Error:', e);
        setIsSpeaking(false);
      };

      window.speechUtterances.push(utterance);
      window.speechSynthesis.speak(utterance);
      
      // Safety timeout to reset speaking state if onend doesn't fire
      setTimeout(() => {
        setIsSpeaking(false);
      }, Math.max(8000, speechText.length * 150));
    };

    if (message.audioBase64) {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }

      const audio = new Audio(`data:audio/mpeg;base64,${message.audioBase64}`);
      audio.volume = 1.0;
      audioRef.current = audio;

      audio.onplay = () => setIsSpeaking(true);
      audio.onended = () => {
        setIsSpeaking(false);
        audioRef.current = null;
      };
      audio.onerror = (e) => {
        console.error('Audio playback error', e);
        audioRef.current = null;
        setIsSpeaking(false);
        playBrowserSpeech();
      };

      audio.play().catch((e) => {
        console.error('Audio playback error', e);
        audioRef.current = null;
        setIsSpeaking(false);
        playBrowserSpeech();
      });
      return;
    }

    playBrowserSpeech();
  }, [ttsEnabled]);

  /* ── Toggle TTS ── */
  const toggleTts = useCallback(() => {
    if (isSpeaking) {
      window.speechSynthesis.cancel();
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      window.speechUtterances = [];
      setIsSpeaking(false);
    }
    setTtsEnabled(prev => !prev);
  }, [isSpeaking]);

  /* ── Scroll to bottom on new messages ── */
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  /* ── Pre-load TTS voices ── */
  useEffect(() => {
    window.speechSynthesis.getVoices();
    window.speechSynthesis.onvoiceschanged = () => {
      window.speechSynthesis.getVoices();
    };
  }, []);

  /* ── Send message to backend ── */
  const handleSendMessage = async (customInput = null) => {
    const textToSend = customInput || input;
    if (!textToSend.trim()) {
      return;
    }

    const userMessage = { id: Date.now(), text: textToSend, sender: 'user' };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsTyping(true);

    try {
      // Rule 2: No manual selection. We only send the message.
      // The AI will auto-detect everything.
      const response = await fetch('/api/chat/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: textToSend })
      });
      const data = await response.json();
      const text = data.responseText || data.response || 'No response generated.';
      const lang = data.language || 'en';
      const nlSummary = data.nlSummary || null;
      const audioBase64 = data.audioBase64 || null;

      const botMessage = {
        id: Date.now() + 1,
        text,
        sender: 'bot',
        detectedLang: lang,
        nlSummary,
        audioBase64
      };
      setMessages((prev) => [...prev, botMessage]);
      speakResponse(botMessage, lang);
    } catch (error) {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          text: 'Connectivity issue. Please check your internet or server status.',
          sender: 'bot'
        }
      ]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className="app-shell">
      <div className="chat-container">
        <header className="chat-header">
          <div className="logo-section">
            <div className="icon-badge">
              <Train size={28} />
            </div>
            <div>
              <h1>RailBot AI</h1>
              <div className="status-badge">
                <span className="pulsing-dot"></span>
                Intelligent & Multilingual
              </div>
            </div>
          </div>
          <button
            className={`theme-toggle ${ttsEnabled ? '' : 'tts-off'}`}
            onClick={toggleTts}
            title={ttsEnabled ? 'Disable voice responses' : 'Enable voice responses'}
          >
            {ttsEnabled ? <Volume2 size={20} /> : <VolumeX size={20} />}
            {isSpeaking && <span className="speaking-indicator"></span>}
          </button>
        </header>

        <main className="chat-messages">
          {messages.map((msg) => (
            <div key={msg.id} className={`message-wrapper ${msg.sender}`}>
              <div className="avatar">
                {msg.sender === 'bot' ? <Bot size={18} /> : <User size={18} />}
              </div>
              <div className="message-container">
                <div className={`message-bubble ${msg.sender}`}>
                  {msg.sender === 'bot' ? (
                    <BotMessage
                      text={msg.text}
                      nlSummary={msg.nlSummary}
                      language={msg.language}
                    />
                  ) : (
                    msg.text
                  )}
                </div>
                <button 
                  className="manual-speak-btn"
                  onClick={() => speakResponse(msg, msg.language || 'en', true)}
                  title="Speak this message"
                >
                  <Volume2 size={14} />
                </button>
              </div>
            </div>
          ))}
          {isTyping && (
            <div className="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          )}
          <div ref={messagesEndRef} />
        </main>

        <footer className="chat-input-area">
          {/* Active Speaking Indicator with Manual Stop */}
          {isSpeaking && (
            <div className="speaking-active-bar">
              <div className="speaking-wave">
                <span className="wave-bar"></span><span className="wave-bar"></span><span className="wave-bar"></span>
              </div>
              <span className="speaking-label">Speaking...</span>
              <button 
                className="stop-speaking-btn" 
                onClick={() => {
                  window.speechSynthesis.cancel();
                  if (audioRef.current) {
                    audioRef.current.pause();
                    audioRef.current = null;
                  }
                  window.speechUtterances = [];
                  setIsSpeaking(false);
                }}
              >
                 Stop
              </button>
            </div>
          )}

          {/* Voice waveform overlay */}
          {isListening && (
            <div className="voice-listening-bar">
              <VoiceWaveform />
              <span className="listening-label">Listening... Speak now</span>
            </div>
          )}
          <div className="input-wrapper">
            <div className="language-selector">
              <select 
                value={recognitionLang} 
                onChange={(e) => setRecognitionLang(e.target.value)}
                title="Select input language for voice"
              >
                {RECOGNITION_OPTIONS.map((option) => (
                  <option key={option.code} value={option.speechCode}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <button
              className={`voice-btn ${isListening ? 'listening' : ''}`}
              onClick={isListening ? stopListening : startListening}
              title={isListening ? 'Stop listening' : 'Speak in any language'}
            >
              {isListening ? <MicOff size={20} /> : <Mic size={20} />}
            </button>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
              placeholder="Type or speak in any language..."
              disabled={isListening}
            />
            <button className="send-btn" onClick={() => handleSendMessage()} disabled={!input.trim() || isListening}>
              <Send size={20} />
            </button>
          </div>
        </footer>
      </div>
    </div>
  );
};

export default App;

