-- ===========================================
-- 1. TRAINS
-- ===========================================
INSERT INTO trains (train_number, train_name) VALUES
('12675', 'Kovai Express'),
('12673', 'Cheran Express'),
('12637', 'Pandian Express'),
('12635', 'Vaigai Express'),
('16526', 'Bangalore Express'),
('12607', 'Lalbagh Express'),
('12027', 'Shatabdi Express'),
('12951', 'Rajdhani Express'),
('20643', 'Vande Bharat Express'),
('12269', 'Duronto Express')
ON CONFLICT (train_number) DO NOTHING;


-- ===========================================
-- 2. STATIONS
-- ===========================================
INSERT INTO stations (code, name_en, name_ta, name_hi) VALUES
('MAS',  'Chennai Central',      'சென்னை சென்ட்ரல்',          'चेन्नई सेंट्रल'),
('CBE',  'Coimbatore Jn',        'கோயம்புத்தூர் சந்திப்பு',     'कोयंबटूर जं'),
('MDU',  'Madurai Jn',           'மதுரை சந்திப்பு',             'मदुरै जं'),
('SBC',  'KSR Bengaluru',        'பெங்களூரு கேஎஸ்ஆர்',         'केएसआर बेंगलुरु'),
('NDLS', 'New Delhi',            'புது தில்லி',                 'नई दिल्ली'),
('MMCT', 'Mumbai Central',       'மும்பை சென்ட்ரல்',            'मुंबई सेंट्रल')
ON CONFLICT (code) DO NOTHING;


-- ===========================================
-- 3. SCHEDULES
-- (sample timings; you can tweak later)
-- ===========================================

-- Kovai Express (12675) MAS -> CBE
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '06:10', '06:15', '1', 0
FROM trains t, stations s
WHERE t.train_number = '12675' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '13:25', '13:30', '2', 0
FROM trains t, stations s
WHERE t.train_number = '12675' AND s.code = 'CBE';


-- Cheran Express (12673) MAS -> CBE
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '22:10', '22:15', '4', 0
FROM trains t, stations s
WHERE t.train_number = '12673' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '06:05', '06:10', '3', 1
FROM trains t, stations s
WHERE t.train_number = '12673' AND s.code = 'CBE';


-- Pandian Express (12637) MAS -> MDU
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '21:20', '21:25', '3', 0
FROM trains t, stations s
WHERE t.train_number = '12637' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '05:45', '05:50', '1', 1
FROM trains t, stations s
WHERE t.train_number = '12637' AND s.code = 'MDU';


-- Vaigai Express (12635) MAS -> MDU
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '13:50', '13:55', '8', 0
FROM trains t, stations s
WHERE t.train_number = '12635' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '21:25', '21:30', '2', 0
FROM trains t, stations s
WHERE t.train_number = '12635' AND s.code = 'MDU';


-- Bangalore Express (16526) MAS -> SBC
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '23:30', '23:35', '5', 0
FROM trains t, stations s
WHERE t.train_number = '16526' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '05:00', '05:05', '7', 1
FROM trains t, stations s
WHERE t.train_number = '16526' AND s.code = 'SBC';


-- Lalbagh Express (12607) MAS -> SBC
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '15:30', '15:35', '6', 0
FROM trains t, stations s
WHERE t.train_number = '12607' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '21:55', '22:00', '2', 0
FROM trains t, stations s
WHERE t.train_number = '12607' AND s.code = 'SBC';


-- Shatabdi Express (12027) MAS -> SBC
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '17:00', '17:05', '2', 0
FROM trains t, stations s
WHERE t.train_number = '12027' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '21:35', '21:40', '5', 0
FROM trains t, stations s
WHERE t.train_number = '12027' AND s.code = 'SBC';


-- Rajdhani Express (12951) NDLS -> MMCT
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '16:55', '17:00', '12', 0
FROM trains t, stations s
WHERE t.train_number = '12951' AND s.code = 'NDLS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '08:35', '08:40', '4', 1
FROM trains t, stations s
WHERE t.train_number = '12951' AND s.code = 'MMCT';


-- Vande Bharat (20643) MAS -> MDU
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '14:25', '14:30', '1', 0
FROM trains t, stations s
WHERE t.train_number = '20643' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '20:45', '20:50', '3', 0
FROM trains t, stations s
WHERE t.train_number = '20643' AND s.code = 'MDU';


-- Duronto (12269) MAS -> SBC
INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '06:10', '06:15', '4', 0
FROM trains t, stations s
WHERE t.train_number = '12269' AND s.code = 'MAS';

INSERT INTO schedules (train_id, station_id, arrival_time, departure_time, platform, day_offset)
SELECT t.id, s.id, '10:45', '10:50', '6', 0
FROM trains t, stations s
WHERE t.train_number = '12269' AND s.code = 'SBC';


-- ===========================================
-- 4. SAMPLE DELAYS
-- ===========================================

-- Kovai Express delayed 20 min at CBE
INSERT INTO delays (train_id, station_id, delay_minutes)
SELECT t.id, s.id, 20
FROM trains t, stations s
WHERE t.train_number = '12675' AND s.code = 'CBE';

-- Cheran Express delayed 5 min at MAS
INSERT INTO delays (train_id, station_id, delay_minutes)
SELECT t.id, s.id, 5
FROM trains t, stations s
WHERE t.train_number = '12673' AND s.code = 'MAS';

-- Pandian on time (no delay row = treated as on time in frontend)
-- Vaigai delayed 10 min at MDU
INSERT INTO delays (train_id, station_id, delay_minutes)
SELECT t.id, s.id, 10
FROM trains t, stations s
WHERE t.train_number = '12635' AND s.code = 'MDU';

-- Vande Bharat cancelled at MDU (use -1 and handle specially in frontend if you want)
INSERT INTO delays (train_id, station_id, delay_minutes)
SELECT t.id, s.id, -1
FROM trains t, stations s
WHERE t.train_number = '20643' AND s.code = 'MDU';
UPDATE train_status SET 
  delay_minutes = 45,
  delay_reason = 'Signal failure near Salem',
  current_location = 'Departed Erode'
WHERE train_number = '12677';

UPDATE train_status SET 
  delay_minutes = 10,
  delay_reason = 'Heavy rain on route',
  current_location = 'Arrived Coimbatore'
WHERE train_number = '12678';

UPDATE train_status SET 
  delay_minutes = NULL,
  delay_reason = 'N/A',
  current_location = 'On time'
WHERE train_number = '12679';
