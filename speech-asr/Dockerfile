FROM python:3.10

WORKDIR /app

# ffmpeg needed for Whisper
RUN apt-get update && apt-get install -y ffmpeg

COPY requirements.txt /app/

RUN pip install --no-cache-dir -r requirements.txt

COPY . /app/

CMD ["python", "whisper_server.py"]
