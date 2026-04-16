from typing import Optional, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import or_, func
from api.core import models


def get_train_status(
    db: Session,
    train_number: str,
    station_code: Optional[str] = None,
) -> Optional[Dict[str, Any]]:

    print("🔍 DEBUG — Train count:", db.query(models.Train).count())
    print("🔍 DEBUG — Station count:", db.query(models.Station).count())
    print("🔍 DEBUG — Schedule count:", db.query(models.Schedule).count())
    print("🔍 DEBUG — Delay count:", db.query(models.Delay).count())
    print("🔍 DEBUG — Requested Train:", train_number)

    # normalize input
    train_identifier = train_number.strip().lower()

    # Find train by number OR name (case insensitive)
    train = (
        db.query(models.Train)
        .filter(
            or_(
                func.lower(models.Train.train_number) == train_identifier,
                func.lower(models.Train.train_name) == train_identifier,
            )
        )
        .first()
    )

    if not train:
        return None

    station = None
    schedule = None
    delay = None

    # If station is provided from chatbot
    if station_code:
        station = (
            db.query(models.Station)
            .filter(models.Station.code == station_code.upper())
            .first()
        )
        if station:
            schedule = (
                db.query(models.Schedule)
                .filter(
                    models.Schedule.train_id == train.id,
                    models.Schedule.station_id == station.id,
                )
                .first()
            )
            delay = (
                db.query(models.Delay)
                .filter(
                    models.Delay.train_id == train.id,
                    models.Delay.station_id == station.id,
                )
                .order_by(models.Delay.last_updated.desc())
                .first()
            )

    # If no station given — pick first stop automatically
    if schedule is None:
        schedule = (
            db.query(models.Schedule)
            .filter(models.Schedule.train_id == train.id)
            .order_by(models.Schedule.id.asc())
            .first()
        )
        if schedule:
            station = schedule.station

    # Always take latest delay for train
    if delay is None:
        delay = (
            db.query(models.Delay)
            .filter(models.Delay.train_id == train.id)
            .order_by(models.Delay.last_updated.desc())
            .first()
        )

    return {
        "train_number": train.train_number,
        "train_name": train.train_name,
        "station_name_en": station.name_en if station else None,
        "arrival_time": schedule.arrival_time if schedule else None,
        "departure_time": schedule.departure_time if schedule else None,
        "platform": schedule.platform if schedule else None,
        "delay_minutes": delay.delay_minutes if delay else None,
        "delay_reason": "Not available",
        "current_location": None,
    }
