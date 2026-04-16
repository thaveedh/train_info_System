import sys

sys.path.insert(0, ".")
import gemini_service

print("=== Running NLP Mock Tests ===")

d1 = gemini_service.detect_language_and_intent("Hello!")
print("Test 1 - Greeting:", d1)
assert d1["language"] == "en", f"FAIL: expected en, got {d1['language']}"
assert d1["intent"] == "GREETING", f"FAIL: expected GREETING, got {d1['intent']}"

d2 = gemini_service.detect_language_and_intent("Where is train 12627?")
print("Test 2 - English train inquiry:", d2)
assert d2["intent"] == "TRAIN_STATUS", f"FAIL: expected TRAIN_STATUS, got {d2['intent']}"
assert d2["trainNumber"] == "12627", f"FAIL: expected 12627, got {d2['trainNumber']}"

d3 = gemini_service.detect_language_and_intent("train enga varuthu 12627")
print("Test 3 - Tanglish train inquiry:", d3)
assert d3["language"] == "ta", f"FAIL: expected ta, got {d3['language']}"
assert d3["intent"] == "TRAIN_STATUS", f"FAIL: expected TRAIN_STATUS, got {d3['intent']}"
assert "ட்ரெயின்" in d3["normalizedText"], "FAIL: Tanglish was not normalized to Tamil script"

d4 = gemini_service.detect_language_and_intent("gaadi kahan hai 12627")
print("Test 4 - Hinglish train inquiry:", d4)
assert d4["language"] == "hi", f"FAIL: expected hi, got {d4['language']}"
assert d4["intent"] == "TRAIN_STATUS", f"FAIL: expected TRAIN_STATUS, got {d4['intent']}"

r4 = gemini_service.format_multilingual_response("Train 12627 delayed by 15 minutes", "en")
print("Test 5 - English response:", r4)
assert "12627" not in r4, "FAIL: digits should be converted in English response"
assert "15" not in r4, "FAIL: digits should be converted in English response"

r5 = gemini_service.format_multilingual_response("ரயில் 12627 தாமதம் 15 நிமிடம்", "ta")
print("Test 6 - Tamil response:", r5)
assert "12627" not in r5, "FAIL: train digits should be converted in Tamil response"
assert "15" not in r5, "FAIL: delay digits should be converted in Tamil response"
assert any("\u0B80" <= ch <= "\u0BFF" for ch in r5), "FAIL: expected Tamil script output"

r6 = gemini_service.format_multilingual_response("Train 12627 delayed by 15 minutes", "hi")
print("Test 7 - Hindi response:", r6)
assert "12627" not in r6, "FAIL: digits should be converted in Hindi response"
assert "15" not in r6, "FAIL: digits should be converted in Hindi response"

print()
print("=== ALL TESTS PASSED ===")
