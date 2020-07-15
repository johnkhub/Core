INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('weight_kg', 'Kilogram', true,'kg', 'T_MASS') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('length_m', 'Meter', true,'m', 'T_LENGTH') ON CONFLICT(code) DO NOTHING;
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

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('length_cm', 'Centimeter', true,'m', 'T_LENGTH') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('length_mm', 'Millimeter', true,'m', 'T_LENGTH') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('length_km', 'Kilometer', true,'m', 'T_LENGTH') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('area_mm2', 'Square millimeter', true, 'm㎡', 'T_AREA') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('area_cm2', 'Square centimeter', true, 'c㎡', 'T_AREA') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('area_km2', 'Square kilometer', true, 'k㎡', 'T_AREA') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_ml', 'Milliliter', true, 'ml', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_l', 'Liter', true, 'l', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_kl', 'Kiloliter', true, 'kl', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_cm3', 'Cubic centimeter', true, 'c㎥', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('volume_dm3', 'Cubic decimeter', true, 'd㎥', 'T_VOLUME') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('current_a', 'Ampere', true,'A', 'T_CURRENT') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('voltage_v', 'Volt', true,'V', 'T_VOLTAGE') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('power_kw', 'Kilowatt', true,'W', 'T_POWER') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('power_kva', 'Kilovolt Ampere', true,'kVA', 'T_POWER') ON CONFLICT(code) DO NOTHING;
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('power_mva', 'Megavolt Ampere', true,'MVA', 'T_POWER') ON CONFLICT(code) DO NOTHING;

INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('temp_c', 'Celsius', true, '℃', 'T_TEMPERATURE') ON CONFLICT(code) DO NOTHING;
