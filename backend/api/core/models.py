from sqlalchemy import (
    Column,
    Integer,
    String,
    Time,
    ForeignKey,
    DateTime,
)
from sqlalchemy.orm import relationship
from .db import Base


class Train(Base):
    __tablename__ = "trains"

    id = Column(Integer, primary_key=True, index=True)
    train_number = Column(String(10), unique=True, index=True, nullable=False)
    train_name = Column(String(100), nullable=False)

    schedules = relationship("Schedule", back_populates="train")
    delays = relationship("Delay", back_populates="train")


class Station(Base):
    __tablename__ = "stations"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(10), unique=True, index=True, nullable=False)
    name_en = Column(String(100), nullable=False)
    name_ta = Column(String(100))
    name_hi = Column(String(100))

    schedules = relationship("Schedule", back_populates="station")
    delays = relationship("Delay", back_populates="station")


class Schedule(Base):
    __tablename__ = "schedules"

    id = Column(Integer, primary_key=True, index=True)
    train_id = Column(Integer, ForeignKey("trains.id"), nullable=False)
    station_id = Column(Integer, ForeignKey("stations.id"), nullable=False)
    arrival_time = Column(Time)
    departure_time = Column(Time)
    platform = Column(String(10))
    day_offset = Column(Integer, default=0)

    train = relationship("Train", back_populates="schedules")
    station = relationship("Station", back_populates="schedules")


class Delay(Base):
    __tablename__ = "delays"

    id = Column(Integer, primary_key=True, index=True)
    train_id = Column(Integer, ForeignKey("trains.id"), nullable=False)
    station_id = Column(Integer, ForeignKey("stations.id"), nullable=False)
    delay_minutes = Column(Integer)
    last_updated = Column(DateTime)

    train = relationship("Train", back_populates="delays")
    station = relationship("Station", back_populates="delays")
