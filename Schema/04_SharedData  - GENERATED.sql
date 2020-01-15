CREATE TYPE "unit_type" AS ENUM (
  'T_TIME',
  'T_LENGTH',
  'T_MASS',
  'T_CURRENT',
  'T_TEMPERATURE',
  'T_LUMINOSITY',
  'T_VOLTAGE',
  'T_POWER',
  'T_VOLUME',
  'T_AREA',
  'T_CURRENCY',
  'T_VELOCITY',
  'T_DENSITY',
  'T_PRESSURE'
);

CREATE TABLE "supplier" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar UNIQUE NOT NULL,
  "description" varchar,
  "supplier_type_code" varchar(10) NOT NULL
);

CREATE TABLE "supplier_type" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar
);

CREATE TABLE "unit" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL,
  "is_si" boolean NOT NULL,
  "symbol" varchar NOT NULL,
  "type" unit_type
);

CREATE TABLE "facility" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar,
  "facility_type_code" varchar(10) NOT NULL
);

CREATE TABLE "facility_type" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar
);

CREATE TABLE "asset_facility" (
  "asset_id" uuid,
  "facility_code" varchar(10),
  PRIMARY KEY ("asset_id", "facility_code")
);

ALTER TABLE "supplier" ADD FOREIGN KEY ("supplier_type_code") REFERENCES "supplier_type" ("code");

ALTER TABLE "facility" ADD FOREIGN KEY ("facility_type_code") REFERENCES "facility_type" ("code");

ALTER TABLE "asset_facility" ADD FOREIGN KEY ("facility_code") REFERENCES "facility" ("code");

CREATE INDEX ON "supplier" ("code");

CREATE INDEX ON "facility" ("code");

CREATE INDEX ON "facility_type" ("code");

CREATE UNIQUE INDEX ON "asset_facility" ("asset_id", "facility_code");

COMMENT ON COLUMN "unit"."is_si" IS 'Is SI or Imperial?';
