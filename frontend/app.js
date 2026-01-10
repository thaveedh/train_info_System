// =======================
// Config
// =======================
const API_BASE_URL = "http://10.70.41.96:8000";
// =======================
// State
// =======================
let recognition = null;
let isRecording = false;
let trains = [];

// =======================
// Language Voices
// =======================
const languageVoices = {
  en: "en-IN",
  ta: "ta-IN",
  hi: "hi-IN"
};
async function autoTranslate(text, targetLang) {
  const res = await fetch(
    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" +
      targetLang +
      "&dt=t&q=" +
      encodeURIComponent(text)
  );
  const data = await res.json();
  return data[0].map(x => x[0]).join("");
}


// =======================
// DOM
// =======================
const messagesEl = document.getElementById("messages");
const inputEl = document.getElementById("user-input");
const sendBtn = document.getElementById("send-btn");
const micBtn = document.getElementById("mic-btn");
const languageSelect = document.getElementById("language-select");

// =======================
// Bot Message
// =======================
function addMessage(text, sender = "bot") {
  const row = document.createElement("div");
  row.className = `message-row ${sender}`;
  const bubble = document.createElement("div");
  bubble.className = "message-bubble";
  bubble.innerText = text;
  row.appendChild(bubble);
  messagesEl.appendChild(row);
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

function getCurrentLanguage() {
  return languageSelect.value;
}

// =======================
// Translation
// =======================
function translate(text, lang) {
  if (lang === "en") return text;

  const dict = {
    ta: {
      "Arrival at": "வருகை",
      "Departure": "புறப்படும் நேரம்",
      "Platform": "தளம்",
      "Delay": "தாமதம்",
      "The train is on time": "ரயில் நேரத்தில் வந்துவிடும்",
      "Current train location": "தற்போதைய ரயில் இருப்பிடம்",
      "Which train are you asking about?": "நீங்கள் எந்த ரயிலை கேட்கிறீர்கள்?",
      "No information available.": "தகவல் கிடைக்கவில்லை",
      "Fetching details...": "தகவல் பெறப்படுகிறது..."
    },
    hi: {
      "Arrival at": "आगमन",
      "Departure": "प्रस्थान",
      "Platform": "प्लेटफॉर्म",
      "Delay": "देरी",
      "The train is on time": "ट्रेन समय पर है",
      "Current train location": "ट्रेन का वर्तमान स्थान",
      "Which train are you asking about?": "आप किस ट्रेन के बारे में पूछ रहे हैं?",
      "No information available.": "सूचना उपलब्ध नहीं है",
      "Fetching details...": "जानकारी प्राप्त की जा रही है..."
    }
  };

  let output = text;
  for (const [en, tr] of Object.entries(dict[lang])) {
    const regex = new RegExp(en, "gi");
    output = output.replace(regex, tr);
  }
  return output;
}


// =======================
// Text-to-Speech
// =======================
let selectedVoice = null;

function speakText(text) {
  if (!("speechSynthesis" in window)) return;

  const lang = getCurrentLanguage();
  const voices = speechSynthesis.getVoices();

  // find best available voice by language
  selectedVoice = voices.find(v => v.lang.startsWith(languageVoices[lang])) || voices[0];

  const utter = new SpeechSynthesisUtterance(text);
  utter.voice = selectedVoice;
  utter.lang = selectedVoice.lang;

  window.speechSynthesis.cancel();
  window.speechSynthesis.speak(utter);
}

// load voices properly on page load
speechSynthesis.onvoiceschanged = () => speechSynthesis.getVoices();


// =======================
// Speech-to-Text
// =======================
function initSpeechRecognition() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    micBtn.disabled = true;
    return;
  }
  recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = false;

  recognition.onstart = () => {
    isRecording = true;
    micBtn.classList.add("recording");
  };
  recognition.onend = () => {
    isRecording = false;
    micBtn.classList.remove("recording");
  };
  recognition.onresult = (e) => {
    inputEl.value = e.results[0][0].transcript;
    handleSend();
  };
}

micBtn.addEventListener("click", () => {
  if (!recognition) return;
  if (!isRecording) {
    recognition.lang = languageVoices[getCurrentLanguage()] || "en-IN";
    recognition.start();
  } else {
    recognition.stop();
  }
});

// =======================
// Train name extractor
// =======================
function detectTrainName(text) {
  const lower = text.toLowerCase();

  // Tamil → English aliases
  const tamilMap = {
    "கோவை எக்ஸ்பிரஸ்": "kovai express",
    "வைகை எக்ஸ்பிரஸ்": "vaigai express",
    "மதுரை எக்ஸ்பிரஸ்": "madurai express",
  };

  // Hindi → English aliases
  const hindiMap = {
    "कुवई एक्सप्रेस": "kovai express",
    "वागई एक्सप्रेस": "vaigai express",
    "मदुरई एक्सप्रेस": "madurai express",
  };

  // Convert TN/HI text to EN train keyword
  for (const [native, english] of Object.entries(tamilMap)) {
    if (lower.includes(native)) text = english;
  }
  for (const [native, english] of Object.entries(hindiMap)) {
    if (lower.includes(native)) text = english;
  }

  // Normal detection (English names & numbers)
  const low2 = text.toLowerCase();
  for (const t of trains) {
    if (low2.includes(t.train_name.toLowerCase())) return t.train_name;
    if (low2.includes(t.train_number)) return t.train_name;
  }

  return null;
}


// =======================
// Station extractor
// =======================
function extractStation(text) {
  const match = text.toUpperCase().match(/\b[A-Z]{2,4}\b/);
  return match ? match[0] : null;
}

// =======================
// API Call
// =======================
async function fetchInfo(train, station) {
  let url = `${API_BASE_URL}/api/train?train_number=${encodeURIComponent(train)}`;
  if (station) url += `&station_code=${station}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("not found");
  return res.json();
}

// =======================
// Intent Detection
// =======================
function detectIntent(text) {
  text = text.toLowerCase();
  const arrivalWords = ["when", "eppo", "reach", "vanthu", "serum", "arrival", "time", "eta"];
  const delayWords = ["delay", "late", "late ah", "thalludu", "delay ah", "why", "ennachu"];
  const locationWords = ["where", "enga", "location", "iruku", "now", "ippo", "current place"];
  const reasonWords = ["why delay", "reason", "yen delay", "kaaranam"];

  if (reasonWords.some(w => text.includes(w))) return "delay_reason";
  if (delayWords.some(w => text.includes(w))) return "delay_status";
  if (arrivalWords.some(w => text.includes(w))) return "arrival";
  if (locationWords.some(w => text.includes(w))) return "location";

  return "general_status";
}

async function autoTranslateInput(text) {
  const lang = getCurrentLanguage();
  if (lang === "en") return text; // no need to translate
  const res = await fetch(
    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" +
      lang + "&tl=en&dt=t&q=" + encodeURIComponent(text)
  );
  const data = await res.json();
  return data[0].map(x => x[0]).join("");
}

// =======================
// Send Handler
// =======================

async function handleSend() {
  let text = inputEl.value.trim();
  text = await autoTranslateInput(text); // convert Tamil / Hindi to English

  if (!text) return;

  const userLang = getCurrentLanguage(); // ta / hi / en
  addMessage(inputEl.value.trim(), "user");
  inputEl.value = "";

  // Step 1: Translate to English for backend logic
  const englishInput = await autoTranslate(text, "en");

  // Step 2: Detect train
  const train = detectTrainName(englishInput);
  if (!train) {
    const msg = await autoTranslate("Which train are you asking about?", userLang);
    addMessage(msg, "bot");
    speakText(msg);
    return;
  }

  const station = extractStation(englishInput);
  let loading = await autoTranslate("Fetching details...", userLang);
  addMessage("⏳ " + loading, "bot");

  try {
  const data = await fetchInfo(train, station);
  const intent = detectIntent(englishInput);

  let msg = "";

  switch (intent) {
    case "arrival":
      msg = `Arrival at ${data.station_name_en}: ${data.arrival_time}`;
      break;

    case "delay_reason":
      msg = `Delay reason: ${data.delay_reason}`;
      break;

    case "location":
      msg = `Current train location: ${data.current_location || "Not available"}`;
      break;

    case "delay_status":
      msg = data.delay_minutes == null
        ? "The train is on time"
        : `Delay ${data.delay_minutes} mins`;
      break;

    default:
      msg =
        `${data.train_name} (${data.train_number})\n` +
        `Arrival at ${data.station_name_en}: ${data.arrival_time}\n` +
        `Departure: ${data.departure_time}\n` +
        `Platform: ${data.platform}\n` +
        `${data.delay_minutes == null ? "The train is on time" : `Delay ${data.delay_minutes} mins`}`;
  }

  // Translate response to selected language
  const translated = await autoTranslate(msg, userLang);

  addMessage(translated, "bot");
  speakText(translated);
}
catch (err) {
    const msg = await autoTranslate("No information available.", userLang);
    addMessage(msg, "bot");
    speakText(msg);
  }
}


// =======================
// UI Events
// =======================
sendBtn.addEventListener("click", handleSend);
inputEl.addEventListener("keydown", (e) => e.key === "Enter" && handleSend());

// =======================
// Init trains + speech
// =======================
async function init() {
  const res = await fetch(`${API_BASE_URL}/api/trains`);
  trains = await res.json();
  initSpeechRecognition();
  addMessage("Ask me any train status 🎙️ Example: 'vaigai express delay'", "bot");
}

function stopSpeech() {
  window.speechSynthesis.cancel();
}


init();
