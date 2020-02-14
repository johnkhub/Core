CREATE SCHEMA "asset";

CREATE TABLE asset.ref_ward (
) INHERITS (kv_base);

CREATE TABLE asset.ref_suburb (
) INHERITS (kv_base);

CREATE TABLE asset.ref_region (
) INHERITS (kv_base);

CREATE TABLE asset.ref_town (
) INHERITS (kv_base);

CREATE TABLE asset.ref_municipality (
) INHERITS (kv_base);

CREATE TABLE asset.ref_district (
) INHERITS (kv_base);

CREATE TABLE asset.ref_facility_type (
) INHERITS (kv_base);


-- Note: The INHERITED keyword does not allow us to inherit indexes, so we need to define them here
CREATE UNIQUE INDEX ON "asset"."ref_ward" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_suburb" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_region" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_town" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_municipality" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_district" ("k");
CREATE UNIQUE INDEX ON "asset"."ref_facility_type" ("k");

--CREATE UNIQUE INDEX ON "asset"."ref_ward" ("v");
--CREATE UNIQUE INDEX ON "asset"."ref_suburb" ("v");
CREATE UNIQUE INDEX ON "asset"."ref_region" ("v");
--CREATE UNIQUE INDEX ON "asset"."ref_town" ("v");
--CREATE UNIQUE INDEX ON "asset"."ref_municipality" ("v");

CREATE UNIQUE INDEX ON "asset"."ref_district" ("v");
CREATE UNIQUE INDEX ON "asset"."ref_facility_type" ("v");

INSERT INTO kv_type (code,name,"table") VALUES ('SUBURB', 'Suburb', 'asset.ref_suburb');
INSERT INTO kv_type (code,name,"table") VALUES ('WARD', 'Ward', 'asset.ref_ward');
INSERT INTO kv_type (code,name,"table") VALUES ('REGION', 'Region', 'asset.ref_region');
INSERT INTO kv_type (code,name,"table") VALUES ('TOWN', 'Town', 'asset.ref_town');
INSERT INTO kv_type (code,name,"table") VALUES ('MUNIC', 'Municiplaity', 'asset.ref_municipality');
INSERT INTO kv_type (code,name,"table") VALUES ('DISTRICT', 'District', 'asset.ref_district');