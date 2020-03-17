DROP TABLE IF EXISTS land_parcel_import;
CREATE TABLE IF NOT EXISTS land_parcel_import (
    "AssetId" text NOT NULL,
    "ClientAssetID" text, -- IGNORED
    "LocationSGcode FROM FAR" text
);

DELETE FROM land_parcel_import;
COPY land_parcel_import 
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/LPI Land Parcels linked to AssetID_V2_20200317.csv' DELIMITER '|' CSV HEADER;

DELETE FROM land_parcel_import WHERE "LocationSGcode FROM FAR" IS NULL;
DELETE FROM land_parcel_import WHERE "LocationSGcode FROM FAR" NOT LIKE 'C%';
DELETE FROM land_parcel_import WHERE "AssetId" IS NULL;


--DELETE FROM "asset"."asset_landparcel";
--DELETE FROM asset.a_tp_landparcel ;
--DELETE FROM asset WHERE asset_type_code = 'LANDPARCEL';

UPDATE land_parcel_import 
SET "AssetId" = REGEXP_REPLACE("AssetId", '\s', '', 'g'), "LocationSGcode FROM FAR" =  REGEXP_REPLACE("LocationSGcode FROM FAR" , '\s', '', 'g');


INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT DISTINCT ON ("LocationSGcode FROM FAR")
	uuid_generate_v4(), 
	"AssetId"||'.'||"LocationSGcode FROM FAR",
	'Parcel ' || "LocationSGcode FROM FAR", 
	text2ltree("AssetId"||'.'||"LocationSGcode FROM FAR"), 
	'LANDPARCEL'
FROM land_parcel_import WHERE NOT EXISTS(SELECT asset_id FROM asset WHERE name = 'Parcel ' || "LocationSGcode FROM FAR" AND asset_type_code = 'LANDPARCEL');

INSERT INTO asset.a_tp_landparcel (asset_id,lpi)
SELECT 
	DISTINCT ON ("AssetId", "LocationSGcode FROM FAR")
    (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LocationSGcode FROM FAR")) AS asset_id, "LocationSGcode FROM FAR" as lpi
FROM land_parcel_import
WHERE (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"LocationSGcode FROM FAR")) IS NOT NULL
ORDER BY "AssetId", "LocationSGcode FROM FAR";


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
	(SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "LocationSGcode FROM FAR") as landparcel_asset_id
FROM land_parcel_import;


--select sum(split.c)
--from
--				(select "AssetId",
--						count("AssetId") as c
--					from land_parcel_import
--					group by "AssetId"
--					having count("AssetId") = 1
--					order by "AssetId") split;


