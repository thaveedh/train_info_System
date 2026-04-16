from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import gemini_service
import re

# FastAPI Entry Point
# This service acts as the AI Brain, processing language and generating responses.

app = FastAPI()

class ChatRequest(BaseModel):
    message: str

class ResponseRequest(BaseModel):
    data: str
    language: str

class TtsRequest(BaseModel):
    text: str
    language: str

@app.post("/process")
async def process_nlp(request: ChatRequest):
    """
    Step 1: Detect language, intent, and train number.
    Calls Gemini to analyze the message.
    """
    try:
        analysis = gemini_service.detect_language_and_intent(request.message)
        
        # Double check train number with Regex if Gemini missed it
        if not analysis.get("trainNumber"):
            match = re.search(r'\d{5}', request.message)
            if match:
                analysis["trainNumber"] = match.group()
                
        return analysis
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/respond")
async def generate_response(request: ResponseRequest):
    """
    Step 2: Take raw train data and convert it to a natural language response.
    Ensures the response is in the same language the user used.
    """
    try:
        response_text = gemini_service.format_multilingual_response(request.data, request.language)
        return {"response": response_text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/tts")
async def get_tts_audio(request: TtsRequest):
    """
    Generate high-quality speech for the given text and language using gTTS.
    Acts as an alternate way to speak Tamil naturally.
    """
    try:
        audio = gemini_service.generate_tts_audio(request.text, request.language)
        if not audio:
            raise HTTPException(status_code=500, detail="Failed to generate audio")
        return {"audio": audio}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
