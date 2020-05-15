-- 
-- Import Envelopes, Facilities, Buildings, Sites, Floors and Rooms into the "asset" table
---

UPDATE asset_import SET "Code (uk)" = REGEXP_REPLACE("Code (uk)", 'F_', '', 'g') WHERE "Level" = 'Facility'; -- remove F prefix from Facility
UPDATE asset_import SET "Code (uk)" = "AssetID" || '-' || "Code (uk)" WHERE "Level" <> 'Asset';

/*
-- This is a temporary constraint for importing DTPW - in general we are not guaranteed that we will have all levels of the tree
ALTER TABLE asset DROP CONSTRAINT IF EXISTS check_paths;
ALTER TABLE asset ADD CONSTRAINT check_paths CHECK (
	(asset_type_code = 'ENVELOPE' AND nlevel(func_loc_path) = 1) OR 
	(asset_type_code = 'FACILITY' AND nlevel(func_loc_path) = 2) OR
	(asset_type_code = 'LANDPARCEL' AND nlevel(func_loc_path) = 2) OR
	(asset_type_code = 'BUILDING' AND nlevel(func_loc_path) = 3) OR
	(asset_type_code = 'SITE' AND nlevel(func_loc_path) = 3) OR
	
	--OR
	--(asset_type_code = 'FLOOR' AND nlevel(func_loc_path) = 4) OR
	--(asset_type_code = 'ROOM' AND nlevel(func_loc_path) = 5)
	
	(asset_type_code = 'FLOOR') OR
	(asset_type_code = 'ROOM')
);
*/

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "AssetID"), uuid_generate_v4()), "AssetID", "Code Name", text2ltree("AssetID"), 'ENVELOPE'
FROM asset_import WHERE "Level" = 'Asset' 
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;




DELETE FROM asset.a_tp_envelope;
DELETE FROM asset.a_tp_building;
DELETE FROM asset.a_tp_component;
DELETE FROM asset.a_tp_facility;
DELETE FROM asset.a_tp_floor;
DELETE FROM asset.a_tp_room;
DELETE FROM asset.a_tp_site;

DELETE FROM asset_classification;



INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	uuid_generate_v4(), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'FACILITY'
FROM asset_import WHERE "Level" = 'Facility' AND asset_import.asset_id IS NULL
ON CONFLICT DO NOTHING
ORDER BY "Code (uk)";


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	uuid_generate_v4(), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'BUILDING'
FROM asset_import WHERE "Level" = 'Building' AND asset_import.asset_id IS NULL
ORDER BY "Code (uk)";

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	uuid_generate_v4(), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'SITE'
FROM asset_import WHERE "Level" = 'Site' AND asset_import.asset_id IS NULL
ORDER BY "Code (uk)";


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	uuid_generate_v4(), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'FLOOR'
FROM asset_import WHERE "Level" = 'Floor' AND asset_import.asset_id IS NULL
ORDER BY "Code (uk)";



INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	uuid_generate_v4(), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'ROOM'
FROM asset_import WHERE "Level" = 'Room' AND asset_import.asset_id IS NULL
ORDER BY "Code (uk)";


UPDATE asset 
set
	code = "Code (uk)",
	name = "Code Name", 
	func_loc_path = text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 
	asset_type_code = 'FACILITY'
FROM asset_import WHERE "Level" = 'Facility' and asset_import.asset_id  = asset.asset_id;


UPDATE asset 
set
	code = "Code (uk)",
	name = "Code Name", 
	func_loc_path = text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 
	asset_type_code = 'BUILDING'
FROM asset_import WHERE "Level" = 'Building' and asset_import.asset_id  = asset.asset_id;


UPDATE asset 
set
	code = "Code (uk)",
	name = "Code Name", 
	func_loc_path = text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 
	asset_type_code = 'SITE'
FROM asset_import WHERE "Level" = 'Site' and asset_import.asset_id  = asset.asset_id;

UPDATE asset 
set
	code = "Code (uk)",
	name = "Code Name", 
	func_loc_path = text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 
	asset_type_code = 'FLOOR'
FROM asset_import WHERE "Level" = 'Floor' and asset_import.asset_id  = asset.asset_id;


UPDATE asset 
set
	code = "Code (uk)",
	name = "Code Name", 
	func_loc_path = text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 
	asset_type_code = 'ROOM'
FROM asset_import WHERE "Level" = 'Room' and asset_import.asset_id  = asset.asset_id;



INSERT INTO location (asset_id, latitude, longitude)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "Code (uk)") as asset_id,
	to_number(REPLACE("Y", ',','.'), '99.9999999999'::text) AS latitude ,
	to_number(REPLACE("X", ',','.')::text, '99.9999999999'::text) AS longitude 
FROM 
	asset_import 
WHERE "Y" IS NOT NULL AND "X" IS NOT NULL
ON CONFLICT (asset_id) do nothing;


UPDATE location 
SET
	latitude = to_number(REPLACE("Y", ',','.'), '99.9999999999'::text),
	longitude = to_number(REPLACE("X", ',','.')::text, '99.9999999999'::text) 
FROM asset_import 
WHERE  "Y" IS NOT NULL AND "X" IS NOT NULL AND location.asset_id = (SELECT asset_id FROM asset WHERE code = "Code (uk)");


INSERT INTO location (asset_id, address)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "Code (uk)"),
	"LocationAddress" 
FROM 
	asset_import 
WHERE "LocationAddress" IS NOT NULL
ON CONFLICT (asset_id) do nothing;

UPDATE location 
SET
	address = "LocationAddress"
FROM asset_import 
WHERE "LocationAddress" IS NOT NULL AND location.asset_id = (SELECT asset_id FROM asset WHERE code = "Code (uk)");


INSERT INTO geoms (asset_id, geom)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "Code (uk)") as asset_id,
	ST_Force3D(ST_GeomFromText("Geometry", 4326)) as geom
FROM 
	asset_import 
WHERE ("Geometry" IS NOT NULL) AND ("Geometry" != '#N/A')
ON CONFLICT (asset_id) DO NOTHING;

UPDATE geoms
SET 
	geom = ST_Force3D(ST_GeomFromText("Geometry", 4326))
FROM 
	asset_import 
WHERE ("Geometry" IS NOT NULL) AND ("Geometry" != '#N/A')  AND location.asset_id  = (SELECT asset_id FROM asset WHERE code = "Code (uk)");




INSERT INTO asset.a_tp_envelope (
	asset_id,
	municipality_code,
	town_code,
	suburb_code,
	district_code
)
SELECT
	DISTINCT ON (asset_id)
	(SELECT asset_id FROM asset WHERE code = "AssetID") AS asset_id,
 	(SELECT k FROM asset.ref_municipality WHERE v = "LOCAL") AS municipality_code,
 	(SELECT k FROM asset.ref_town WHERE v = "TOWN") AS town_code,
	"Suburb_Id" AS suburb_code,
	(SELECT k FROM asset.ref_district WHERE v = "DISTRICT") AS district_code
FROM asset_import
WHERE "Level" = 'Asset'
ORDER BY asset_id ASC;


INSERT INTO "asset"."a_tp_facility" (asset_id,facility_type_code)
SELECT
	A.asset_id,
	"AssetTypeID" AS facility_type_code
FROM asset_import I join asset A on  ("AssetTypeID" IS NOT null) and ("Level" = 'Facility') and I."Code (uk)" = A.code;


INSERT INTO asset_classification(asset_id,responsible_dept_code)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "Code (uk)") as asset_id,
	"Department"  as responsible_dept_code
FROM 
	asset_import 
WHERE "Department" IS NOT null and ((SELECT asset_id FROM asset WHERE code = "Code (uk)") = asset_id)
ON CONFLICT (asset_id) DO
UPDATE SET responsible_dept_code = EXCLUDED.responsible_dept_code; 



INSERT INTO asset_classification(asset_id,is_owned)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "Code (uk)") as asset_id,
	"Owned"  as is_owned
FROM 
	asset_import 
WHERE "Owned" IS NOT null and ((SELECT asset_id FROM asset WHERE code = "Code (uk)") = asset_id)
ON CONFLICT (asset_id) DO
UPDATE SET is_owned = EXCLUDED.is_owned; 