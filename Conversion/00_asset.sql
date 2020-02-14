SELECT '========================== 00_asset.sql ===========================';
-- 
-- Import Envelopes, Facilities, Buildings, Sites, Floors and Rooms into the "asset" table
---

UPDATE asset_import SET "Code (uk)" = REGEXP_REPLACE("Code (uk)", 'F_', '', 'g') WHERE "Level" = 'Facility'; -- remove F prefix from Facility
UPDATE asset_import SET "Code (uk)" = "AssetID" || '-' || "Code (uk)" WHERE "Level" <> 'Asset';

-- This is a temporary constraint for importing DTPW - in general we are not guaranteed that we will have all levels of the tree
ALTER TABLE asset ADD CONSTRAINT check_paths CHECK (
	(asset_type_code = 'ENVELOPE' AND nlevel(func_loc_path) = 1) OR 
	(asset_type_code = 'FACILITY' AND nlevel(func_loc_path) = 2) OR
	(asset_type_code = 'LANDPARCEL' AND nlevel(func_loc_path) = 2) OR
	(asset_type_code = 'BUILDING' AND nlevel(func_loc_path) = 3) OR
	(asset_type_code = 'SITE' AND nlevel(func_loc_path) = 3) OR
	/*
	OR
	(asset_type_code = 'FLOOR' AND nlevel(func_loc_path) = 4) OR
	(asset_type_code = 'ROOM' AND nlevel(func_loc_path) = 5)
	*/
	(asset_type_code = 'FLOOR') OR
	(asset_type_code = 'ROOM')
);

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "AssetID"), uuid_generate_v4()), "AssetID", "Facility/Asset Name", text2ltree("AssetID"), 'ENVELOPE'
FROM asset_import WHERE "Level" = 'Asset' 
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "Code (uk)"), uuid_generate_v4()), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'FACILITY'
FROM asset_import WHERE "Level" = 'Facility'
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "Code (uk)"), uuid_generate_v4()), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'BUILDING'
FROM asset_import WHERE "Level" = 'Building'
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "Code (uk)"), uuid_generate_v4()), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'SITE'
FROM asset_import WHERE "Level" = 'Site'
ORDER BY "Code (uk)"
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "Code (uk)"), uuid_generate_v4()), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'FLOOR'
FROM asset_import WHERE "Level" = 'Floor'
ORDER BY "Code (uk)"
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT
	COALESCE((SELECT asset_id FROM asset WHERE code = "Code (uk)"), uuid_generate_v4()), "Code (uk)", "Code Name", text2ltree(REGEXP_REPLACE("Code (uk)", '-', '.', 'g')), 'ROOM'
FROM asset_import WHERE "Level" = 'Room' 
ORDER BY "Code (uk)"
ON CONFLICT (asset_id) DO
UPDATE SET name = EXCLUDED.name, func_loc_path = EXCLUDED.func_loc_path, asset_type_code = EXCLUDED.asset_type_code;


INSERT INTO location (asset_id, latitude, longitude)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "AssetID") as asset_id,
	to_number(REPLACE("Y", ',','.'), '99.9999999999'::text) AS latitude ,
	to_number(REPLACE("X", ',','.')::text, '99.9999999999'::text) AS longitude 
FROM 
	asset_import 
WHERE "Y" IS NOT NULL AND "X" IS NOT NULL
ON CONFLICT (asset_id) DO
UPDATE SET latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude;



-- This is an upsert of address
INSERT INTO location (asset_id, address)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "AssetID") as asset_id,
	"LocationAddress" AS address
FROM 
	asset_import 
WHERE "LocationAddress" IS NOT NULL
ON CONFLICT (asset_id) DO UPDATE SET address = EXCLUDED.address;


INSERT INTO geoms (asset_id, geom)
SELECT 
	(SELECT asset_id FROM asset WHERE code = "AssetID") as asset_id,
	ST_GeomFromText("Geometry") as geom
FROM 
	asset_import 
WHERE ("Geometry" IS NOT NULL) AND ("Geometry" != '#N/A')
ON CONFLICT (asset_id) DO
UPDATE SET geom = EXCLUDED.geom;


-- serial, barcode go here 