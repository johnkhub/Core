CREATE TABLE "asset_classification" (
  "asset_id" uuid PRIMARY KEY,
  "responsible_dept_code" varchar(10) NOT NULL
);
ALTER TABLE "asset_classification" ADD FOREIGN KEY ("responsible_dept_code") REFERENCES "dtpw"."ref_client_department" ("k");


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

CREATE TABLE postal_code (
  id serial PRIMARY KEY,
  suburb text,
  box_code text,
  street_code text, 
  area text
);

 CREATE INDEX ON postal_code (suburb,box_code);
 CREATE INDEX ON postal_code (suburb,street_code);

/*
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

ALTER TABLE "supplier" ADD FOREIGN KEY ("supplier_type_code") REFERENCES "supplier_type" ("code");

CREATE INDEX ON "supplier" ("code");
*/


CREATE TABLE "unit" (
  "code" varchar(10) UNIQUE PRIMARY KEY CHECK(code ~'^[\w]*$'),
  "name" varchar NOT NULL CHECK(name  <> ''),
  "is_si" boolean NOT NULL,
  "symbol" varchar NOT NULL CHECK(symbol  <> ''),
  "type" unit_type
);
COMMENT ON COLUMN "unit"."is_si" IS 'Is SI or Imperial?';


-- Consider making an AssetExt base table to be inherited
CREATE TABLE "asset"."a_tp_envelope" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar,
  -- No need for checks as we have FK constraint on lookup tables
  "municipality_code" varchar(10) NULL,
  "town_code" varchar(10) NULL,
  "suburb_code" varchar(10) NULL,
  "district_code" varchar(10) NULL,
  "region_code" varchar(10) NULL,
  "ward_code" varchar(10) NULL
);
COMMENT ON TABLE "asset"."a_tp_envelope" IS 'Asset extension table for Asset Type ENVELOPE';


CREATE TABLE "asset"."a_tp_landparcel" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "lpi" varchar(21) UNIQUE NULL CHECK(char_length(lpi) = 21),
  "description" varchar
);
COMMENT ON TABLE "asset"."a_tp_landparcel" IS 'Asset extension table for Asset Type LANDPARCEL';
COMMENT ON COLUMN "asset"."a_tp_landparcel"."lpi" IS 'Land Parcel Identifier';

CREATE TABLE "asset"."a_tp_facility" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar,
  "facility_type_code" varchar(10) NOT NULL
);
COMMENT ON TABLE "asset"."a_tp_facility" IS 'Asset extension table for Asset Type FACILITY';

CREATE TABLE "asset"."a_tp_building" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar
);
COMMENT ON TABLE "asset"."a_tp_building" IS 'Asset extension table for Asset Type BUILDING';

CREATE TABLE "asset"."a_tp_site" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar
);
COMMENT ON TABLE "asset"."a_tp_site" IS 'Asset extension table for Asset Type SITE';

CREATE TABLE "asset"."a_tp_floor" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar
);
COMMENT ON TABLE "asset"."a_tp_floor" IS 'Asset extension table for Asset Type FLOOR';

CREATE TABLE "asset"."a_tp_room" (
  "asset_id" uuid  UNIQUE PRIMARY KEY,
  "description" varchar
);
COMMENT ON TABLE "asset"."a_tp_room" IS 'Asset extension table for Asset Type ROOM';

CREATE TABLE "asset"."a_tp_component" (
  "asset_id" uuid  UNIQUE PRIMARY KEY
);
COMMENT ON TABLE "asset"."a_tp_component" IS 'Asset extension table for Asset Type COMPONENT';


CREATE TABLE  "asset"."asset_landparcel" (
  "asset_id" uuid ,
  "landparcel_asset_id" uuid,
  PRIMARY KEY("asset_id","landparcel_asset_id")
);
COMMENT ON TABLE "asset"."asset_landparcel" IS 'Linking table between asset and landparcel';

ALTER TABLE "asset"."asset_landparcel" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."asset_landparcel" ADD FOREIGN KEY ("landparcel_asset_id") REFERENCES "asset"."a_tp_landparcel" ("asset_id");

ALTER TABLE "asset"."a_tp_facility" ADD FOREIGN KEY ("facility_type_code") REFERENCES "asset"."ref_facility_type" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("municipality_code") REFERENCES "asset"."ref_municipality" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("town_code") REFERENCES "asset"."ref_town" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("suburb_code") REFERENCES "asset"."ref_suburb" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("district_code") REFERENCES "asset"."ref_district" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("ward_code") REFERENCES "asset"."ref_ward" ("k");
ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("region_code") REFERENCES "asset"."ref_region" ("k");

ALTER TABLE "asset"."a_tp_envelope" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_landparcel" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_facility" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_building" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_site" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_floor" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_room" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset"."a_tp_component" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");