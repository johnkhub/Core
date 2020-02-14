INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('time_kg', 'Kilogram', true,'kg', 'T_MASS') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('lenght_m', 'Meter', true,'m', 'T_LENGTH') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('current_a', 'Ampere', true,'A', 'T_CURRENT') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('voltage_v', 'Volt', true,'V', 'T_VOLTAGE') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('power_w', 'Watt', true,'W', 'T_POWER') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_m', 'Cubic Meter', true, '㎥', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('area_m', 'Square Meter', true, '㎡', 'T_AREA') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('temp_c', 'Celsius', true, '℃', 'T_TEMPERATURE') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('zar', 'Rand', true, 'R', 'T_CURRENCY') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('usd', 'US Dollar', true, '$', 'T_CURRENCY') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('eur', 'Euro', true, '€', 'T_CURRENCY') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('gbp', 'British Pound', true, '£', 'T_CURRENCY') ON CONFLICT(code) DO NOTHING;