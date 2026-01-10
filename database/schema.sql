CREATE TABLE IF NOT EXISTS trains (
    id SERIAL PRIMARY KEY,
    train_number VARCHAR(10) UNIQUE NOT NULL,
    train_name   VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS stations (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_ta VARCHAR(100),
    name_hi VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS schedules (
    id SERIAL PRIMARY KEY,
    train_id INTEGER NOT NULL REFERENCES trains (id) ON DELETE CASCADE,
    station_id INTEGER NOT NULL REFERENCES stations (id) ON DELETE CASCADE,
    arrival_time TIME,
    departure_time TIME,
    platform VARCHAR(10),
    day_offset INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS delays (
    id SERIAL PRIMARY KEY,
    train_id INTEGER NOT NULL REFERENCES trains (id) ON DELETE CASCADE,
    station_id INTEGER NOT NULL REFERENCES stations (id) ON DELETE CASCADE,
    delay_minutes INTEGER,
    last_updated TIMESTAMP DEFAULT NOW()
);
ALTER TABLE train_status ADD COLUMN delay_reason TEXT DEFAULT 'N/A';
ALTER TABLE train_status ADD COLUMN current_location TEXT DEFAULT 'N/A';
