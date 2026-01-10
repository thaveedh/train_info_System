import time
from sqlalchemy import create_engine
from sqlalchemy.exc import OperationalError
from .config import settings

def wait_for_db():
    engine = create_engine(settings.database_url)
    while True:
        try:
            conn = engine.connect()
            conn.close()
            print("Database is ready.")
            break
        except OperationalError:
            print("Database not ready. Retrying in 1 second...")
            time.sleep(1)
