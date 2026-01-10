from fastapi import FastAPI
from pydantic import BaseModel
import base64
import tempfile
import uvicorn
import whisper

app = FastAPI(title="Whisper ASR Server")

# Load Whisper model
model = whisper.load_model("small")   # tiny / base / small / medium / large


class ASRRequest(BaseModel):
    audio: str          # base64 audio string
    language: str = "en"


@app.post("/transcribe")
def transcribe(req: ASRRequest):
    try:
        # decode base64
        audio_data = base64.b64decode(req.audio)

        # save to temporary WAV file
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as f:
            f.write(audio_data)
            audio_path = f.name

        # transcribe
        result = model.transcribe(audio_path, language=req.language)

        return {"text": result["text"]}

    except Exception as e:
        return {"error": str(e)}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5001)
