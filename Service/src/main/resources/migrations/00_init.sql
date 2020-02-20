CREATE EXTENSION IF NOT EXISTS "ltree";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- Type: unit_type

-- DROP TYPE public.unit_type;

CREATE TYPE public.unit_type AS ENUM
    ('T_TIME', 'T_LENGTH', 'T_MASS', 'T_CURRENT', 'T_TEMPERATURE', 'T_LUMINOSITY', 'T_VOLTAGE', 'T_POWER', 'T_VOLUME', 'T_AREA', 'T_CURRENCY', 'T_VELOCITY', 'T_DENSITY', 'T_PRESSURE');

ALTER TYPE public.unit_type
    OWNER TO postgres;