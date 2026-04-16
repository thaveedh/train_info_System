import whisper

model = whisper.load_model("tiny")

def transcribe_audio(audio_bytes, lang="en"):
    with open("temp.wav", "wb") as f:
        f.write(audio_bytes)

    result = model.transcribe("temp.wav")
    return result["text"]
