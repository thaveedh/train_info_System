from typing import Optional
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from api.core.db import get_db
from api.services.train_info_service import get_train_status
from api.core import models

router = APIRouter(prefix="/api", tags=["train-info"])

@router.get("/trains")
def list_trains(db: Session = Depends(get_db)):
    trains = db.query(models.Train).all()
    return [{"train_number": t.train_number, "train_name": t.train_name} for t in trains]

@router.get("/train")
def train_info(
    train_number: str,
    station_code: Optional[str] = None,
    db: Session = Depends(get_db),
):
    result = get_train_status(db, train_number, station_code)
    if result is None:
        raise HTTPException(status_code=404, detail="Train not found")
    return result
