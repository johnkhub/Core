CREATE TABLE "asset" (
  "asset_id" uuid PRIMARY KEY,
  "asset_type_code" varchar(10) NOT NULL,
  "adm_path" varchar,
  "func_loc_path" varchar,
  "grap_path" varchar,
  "creation_date" timestamp NOT NULL DEFAULT 'NOW()',
  "deactivated_at" timestamp,
  "reference_count" BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE "assettype" (
  "code" varchar(10) PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar
);

CREATE TABLE "mapfeature" (
  "asset_id" uuid PRIMARY KEY,
  "mapfeature_type_code" varchar(10),
  "polygon" varchar
);

CREATE TABLE "mapfeature_type" (
  "code" varchar(10) UNIQUE PRIMARY KEY,
  "name" varchar NOT NULL
);

CREATE TABLE "location" (
  "asset_id" uuid UNIQUE PRIMARY KEY,
  "location_path" varchar NOT NULL,
  "latitude" decimal(9,6) NOT NULL,
  "longitude" decimal(9,6) NOT NULL,
  "address" varchar(80)
);

CREATE TABLE "asset_link" (
  "asset_id" uuid,
  "external_id" varchar,
  "external_id_type" uuid NOT NULL,
  PRIMARY KEY ("asset_id", "external_id")
);

CREATE TABLE "external_id_type" (
  "type_id" uuid PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar NOT NULL
);

ALTER TABLE "asset" ADD FOREIGN KEY ("asset_type_code") REFERENCES "assettype" ("code");

ALTER TABLE "mapfeature" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");

ALTER TABLE "mapfeature" ADD FOREIGN KEY ("mapfeature_type_code") REFERENCES "mapfeature_type" ("code");

ALTER TABLE "location" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");

ALTER TABLE "asset_link" ADD FOREIGN KEY ("external_id_type") REFERENCES "external_id_type" ("type_id");

ALTER TABLE "asset_link" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");

CREATE INDEX ON "asset" ("asset_id");

CREATE INDEX ON "asset" ("asset_type_code");

CREATE INDEX ON "asset" ("adm_path");

CREATE INDEX ON "asset" ("func_loc_path");

CREATE INDEX ON "asset" ("grap_path");

CREATE INDEX ON "asset" ("deactivated_at");

CREATE INDEX ON "assettype" ("code");

CREATE INDEX ON "mapfeature_type" ("code");

CREATE INDEX ON "location" ("location_path");

CREATE INDEX ON "asset_link" ("external_id_type", "asset_id", "external_id");

CREATE INDEX ON "asset_link" ("external_id_type", "external_id", "external_id_type");

COMMENT ON COLUMN "asset"."adm_path" IS 'Asset may or may not have one. Links to Template Service';

COMMENT ON COLUMN "asset"."deactivated_at" IS 'WE DO NOT DELETE';

COMMENT ON COLUMN "asset"."reference_count" IS 'We should be allowed to delete terminal nodes that have no transactions against them?';

COMMENT ON COLUMN "external_id_type"."type_id" IS 'E.g. Asset ID in SAP';
