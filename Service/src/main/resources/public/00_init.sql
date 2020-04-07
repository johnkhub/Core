CREATE EXTENSION IF NOT EXISTS "ltree";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

CREATE TYPE public.unit_type AS ENUM
    ('T_TIME', 'T_LENGTH', 'T_MASS', 'T_CURRENT', 'T_TEMPERATURE', 'T_LUMINOSITY', 'T_VOLTAGE', 'T_POWER', 'T_VOLUME', 'T_AREA', 'T_CURRENCY', 'T_VELOCITY', 'T_DENSITY', 'T_PRESSURE');

ALTER TYPE public.unit_type
    OWNER TO postgres;

CREATE FUNCTION table_exists (
    fqn TEXT
)
RETURNS boolean
AS $$
BEGIN
    IF EXISTS (SELECT * FROM information_schema.tables WHERE table_schema = split_part(fqn,'.',1) AND table_name = split_part(fqn,'.',2)) THEN
      RETURN  true;
    ELSE
      RETURN false;
    END IF;
END; $$
LANGUAGE PLPGSQL
;

CREATE SCHEMA access_control;
CREATE SCHEMA audit;
CREATE SCHEMA dtpw;
CREATE SCHEMA asset;
//
