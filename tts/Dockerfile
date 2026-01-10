FROM python:3.10

WORKDIR /app

COPY . /app/

RUN pip install fastapi uvicorn gtts --no-cache-dir

CMD ["python", "tts_server.py"]
