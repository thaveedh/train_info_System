from fastapi import FastAPI
from pydantic import BaseModel
from gtts import gTTS
import os
import uuid
import uvicorn

app = FastAPI(title="TTS Server")

AUDIO_SAVE_PATH = "audio/"
os.makedirs(AUDIO_SAVE_PATH, exist_ok=True)

class TTSRequest(BaseModel):
    text: str
    lang: str = "en"


@app.post("/tts")
def tts_generate(req: TTSRequest):
    try:
        # generate audio file name
        file_id = str(uuid.uuid4())
        filename = f"{AUDIO_SAVE_PATH}{file_id}.mp3"

        # create TTS audio
        tts = gTTS(req.text, lang=req.lang)
        tts.save(filename)

        # return local file URL path
        return {"audio_url": f"/audio/{file_id}.mp3"}

    except Exception as e:
        return {"error": str(e)}


@app.get("/audio/{file}")
def get_audio(file: str):
    file_path = f"{AUDIO_SAVE_PATH}{file}"
    if os.path.exists(file_path):
        return FileResponse(file_path, media_type="audio/mpeg")
    return {"error": "file not found"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5003)
