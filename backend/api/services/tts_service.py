from gtts import gTTS
import os

def generate_tts(text, lang="en"):
    file_path = f"/app/audio/{abs(hash(text))}.mp3"
    tts = gTTS(text, lang="en")
    tts.save(file_path)

    return f"http://localhost:5003/audio/{os.path.basename(file_path)}"
