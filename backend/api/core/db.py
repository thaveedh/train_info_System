from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker
from .config import settings

engine = create_engine(settings.database_url, future=True)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
    future=True,
)

Base = declarative_base()


# Dependency for FastAPI
def get_db():
    from fastapi import Depends  # imported inside to avoid circulars

    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
