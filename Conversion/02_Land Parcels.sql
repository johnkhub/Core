DROP TABLE IF EXISTS land_parcel_import;
CREATE TABLE IF NOT EXISTS land_parcel_import (
    "AssetId" text NOT NULL,
    "ClientAssetID" text NULL, -- IGNORED
    "LPI Code" text NULL,
	"Geometry" text NULL
);

DELETE FROM land_parcel_import;
\copy land_parcel_import FROM '/home/frank/Documents/Naming updates 20200512/LPI Land Parcels linked to AssetID_V3_20200512_Geometry added.csv' DELIMITER '|' CSV HEADER;

DELETE FROM land_parcel_import WHERE "LPI Code" IS NULL;
DELETE FROM land_parcel_import WHERE "LPI Code" NOT LIKE 'C%';
DELETE FROM land_parcel_import WHERE "AssetId" IS NULL;


--DELETE FROM asset WHERE asset_type_code = 'LANDPARCEL';

UPDATE land_parcel_import 
SET "AssetId" = REGEXP_REPLACE("AssetId", '\s', '', 'g'), "LPI Code" =  REGEXP_REPLACE("LPI Code" , '\s', '', 'g');


DELETE FROM "asset"."asset_landparcel";
DELETE FROM asset.a_tp_landparcel;
delete from asset where asset_type_code  = 'LANDPARCEL';

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT   DISTINCT ON ("LPI Code")
    uuid_generate_v4(),
    "AssetId"||'-'||"LPI Code",
    'Parcel ' || "LPI Code",
    text2ltree("AssetId"||'.'||"LPI Code"),
    'LANDPARCEL'
FROM
    land_parcel_import 
WHERE "AssetId" is not null and "LPI Code" is not null





INSERT INTO asset.a_tp_landparcel (asset_id,lpi)
SELECT 
	DISTINCT ON ("AssetId", "LPI Code")
    (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LPI Code")) AS asset_id, "LPI Code" as lpi
FROM land_parcel_import
WHERE (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LPI Code")) IS NOT NULL
ORDER BY "AssetId", "LPI Code";


-- Do not import parcels that are not linked to an envelope
DELETE FROM land_parcel_import WHERE "AssetId" IN (
	SELECT 
		land_parcel_import."AssetId"  FROM asset RIGHT JOIN land_parcel_import ON asset.code = land_parcel_import."AssetId" 
	WHERE 
		asset_id IS NULL
);

INSERT INTO "asset"."asset_landparcel"
SELECT 
	(SELECT asset_id FROM asset WHERE code = "AssetId") as asset_id,
	(SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "LPI Code") as landparcel_asset_id
FROM land_parcel_import
where "LPI Code" is not null 
on conflict (asset_id,landparcel_asset_id) do nothing;



DELETE FROM "asset"."asset_landparcel";
DELETE FROM asset.a_tp_landparcel;

INSERT INTO asset.a_tp_landparcel (asset_id,lpi)
SELECT 
	DISTINCT ON ("AssetId", "LPI Code")
    (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LPI Code")) AS asset_id, "LPI Code" as lpi
FROM land_parcel_import
WHERE (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LPI Code")) IS NOT NULL
ORDER BY "AssetId", "LPI Code";

select *,char_length("LPI Code") from land_parcel_import where (char_length(("LPI Code")::text) > 21)


INSERT INTO "asset"."asset_landparcel"
SELECT 
	(SELECT asset_id FROM asset WHERE code = "AssetId") as asset_id,
	(SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "LPI Code") as landparcel_asset_id
FROM land_parcel_import
where "LPI Code" is not null 
on conflict (asset_id,landparcel_asset_id) do nothing;



update land_parcel_import set "Geometry" = null where char_length(trim("Geometry")) = 0

INSERT INTO geoms (asset_id, geom)
SELECT 
	(SELECT asset_id FROM asset WHERE code =  "AssetId"||'.'||"LPI Code") as asset_id,
	ST_Force3D(ST_GeomFromText("Geometry", 4326)) as geom
FROM 
	land_parcel_import
WHERE ("Geometry" IS NOT NULL) AND ("Geometry" != '#N/A') and ((SELECT asset_id FROM asset WHERE code =  "AssetId"||'.'||"LPI Code") is not null and "LPI Code" is not null)
ON CONFLICT (asset_id) DO NOTHING;





