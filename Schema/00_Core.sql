CREATE TABLE "asset" (
  "asset_id" uuid PRIMARY KEY,
  "asset_type_code" varchar(10) NOT NULL,
  
  "code" varchar(100) UNIQUE,
  "name" varchar NOT NULL,
  
  "adm_path" ltree NULL,
  "func_loc_path" ltree NOT NULL,
  "grap_path" ltree NULL ,
  
  "creation_date" timestamp NOT NULL DEFAULT 'NOW()',
  "deactivated_at" timestamp,
  "reference_count" BIGINT NOT NULL DEFAULT 0
);


-- NOTE: ltree does not provide a means to add referential constraints so deleting a node that is tied to a path segment is failed.

CREATE TABLE "assettype" (
  "code" varchar(10) PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar,
  uid uuid NOT NULL
);
COMMENT ON COLUMN "assettype"."uid" IS 'This field is required to model the assettype as an entity in the context of the access control code (e.g. "access_control"."entity_access" table)';


CREATE TABLE "geoms" (
  "asset_id" uuid PRIMARY KEY,
  "geom" geometry NOT NULL
);


CREATE TABLE "location" (
  "asset_id" uuid UNIQUE PRIMARY KEY,
  "address" text, -- This needs to be replaced with more structured data
 /* 
  "location_path" ltree NULL, -- 

	province 
    region
      district
        municipality
          town
            suburb
              street           
*/


  "latitude" decimal(9,6) ,
  "longitude" decimal(9,6)
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

CREATE TABLE "asset_identification" (
  "asset_id" uuid,
  "barcode" varchar NULL CHECK(barcode  <> ''),
  "serial_number" varchar NULL CHECK(serial_number  <> '')
);

ALTER TABLE "asset" ADD FOREIGN KEY ("asset_type_code") REFERENCES "assettype" ("code");
ALTER TABLE "geoms" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "location" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset_link" ADD FOREIGN KEY ("external_id_type") REFERENCES "external_id_type" ("type_id");
ALTER TABLE "asset_link" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");
ALTER TABLE "asset_identification" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");

CREATE INDEX ON "asset" ("asset_id");
CREATE INDEX ON "asset" ("asset_type_code");
CREATE INDEX ON "asset" ("adm_path");
CREATE INDEX ON "asset" ("grap_path");
CREATE INDEX ON "asset" ("deactivated_at");
CREATE INDEX "func_loc_path_idx" ON "asset" USING gist ("func_loc_path");
CREATE INDEX ON "assettype" ("code");
/*CREATE INDEX ON "location" ("location_path");*/
CREATE INDEX ON "asset_link" ("external_id_type", "asset_id", "external_id");
CREATE INDEX ON "asset_link" ("external_id_type", "external_id", "external_id_type");
CREATE INDEX ON "asset_identification" ("asset_id", "barcode");
CREATE INDEX ON "asset_identification" ("asset_id", "serial_number");

COMMENT ON COLUMN "asset"."adm_path" IS 'Asset may or may not have one. Links to Template Service';
COMMENT ON COLUMN "asset"."deactivated_at" IS 'WE DO NOT DELETE';
COMMENT ON COLUMN "asset"."reference_count" IS 'We should be allowed to delete terminal nodes that have no transactions against them?';
COMMENT ON COLUMN "external_id_type"."type_id" IS 'E.g. Asset ID in SAP';