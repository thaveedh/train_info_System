from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from api.core.db import get_db
from api.core import models

router = APIRouter(prefix="/api", tags=["train-list"])


@router.get("/trains")
def list_trains(db: Session = Depends(get_db)):
    trains = db.query(models.Train).all()
    return [{"train_number": t.train_number, "train_name": t.train_name} for t in trains]
