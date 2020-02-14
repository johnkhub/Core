CREATE TABLE IF NOT EXISTS land_parcel_import (
    "AssetId" text NOT NULL,
    "ClientAssetID" text, -- IGNORED
    "AssetFacilityName" text, -- IGNORED
    "LocationSGcode FROM FAR" text, -- IGNORED
    "MapFeatureID UPDATED BY IMQS" text NOT NULL
);

COPY land_parcel_import 
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/LPI Land Parcels linked to AssetID_V2_20200203.csv' DELIMITER ',' CSV HEADER;

UPDATE land_parcel_import 
SET "AssetId" = REGEXP_REPLACE("AssetId", '\s', '', 'g'), "MapFeatureID UPDATED BY IMQS" =  REGEXP_REPLACE("MapFeatureID UPDATED BY IMQS" , '\s', '', 'g');

DELETE FROM  land_parcel_import WHERE "MapFeatureID UPDATED BY IMQS" NOT LIKE 'C%';

INSERT INTO asset (asset_id, code, name, func_loc_path, asset_type_code)
SELECT DISTINCT ON ("MapFeatureID UPDATED BY IMQS")
	uuid_generate_v4(), 
	"AssetId"||'.'||"MapFeatureID UPDATED BY IMQS",
	'Parcel ' || "MapFeatureID UPDATED BY IMQS", 
	text2ltree("AssetId"||'.'||"MapFeatureID UPDATED BY IMQS"), 
	'LANDPARCEL'
FROM land_parcel_import WHERE NOT EXISTS(SELECT asset_id FROM asset WHERE name = 'Parcel ' || "MapFeatureID UPDATED BY IMQS" AND asset_type_code = 'LANDPARCEL');

INSERT INTO asset.a_tp_landparcel (asset_id,lpi)
SELECT 
	DISTINCT ON ("AssetId", "MapFeatureID UPDATED BY IMQS")
    (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"MapFeatureID UPDATED BY IMQS")) AS asset_id, "MapFeatureID UPDATED BY IMQS" as lpi
FROM land_parcel_import
WHERE (SELECT asset_id FROM asset WHERE func_loc_path = text2ltree("AssetId"||'.'||"MapFeatureID UPDATED BY IMQS")) IS NOT NULL
ORDER BY "AssetId", "MapFeatureID UPDATED BY IMQS";

INSERT INTO "asset"."asset_landparcel"
SELECT 
	DISTINCT ON (	(SELECT asset_id FROM asset WHERE code = "AssetId"),
	(SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "MapFeatureID UPDATED BY IMQS"))
	(SELECT asset_id FROM asset WHERE code = "AssetId") as asset_id,
	(SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "MapFeatureID UPDATED BY IMQS") as landparcel_asset_id
FROM land_parcel_import 
WHERE (SELECT asset_id FROM asset WHERE code = "AssetId") IS NOT NULL AND (SELECT asset_id FROM asset.a_tp_landparcel WHERE lpi = "MapFeatureID UPDATED BY IMQS") IS NOT NULL;
