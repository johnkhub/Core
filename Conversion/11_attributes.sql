SELECT '========================== 11_attributes.sql ===========================';

UPDATE asset_import  SET "SUBURB" = "SUBURB" || '_' || "Suburb_Id" WHERE "SUBURB" = 'BERGSIG';

INSERT INTO asset.ref_suburb (v,k)
	SELECT 
		"SUBURB" as v,
		"Suburb_Id" as k
	FROM asset_import 
	WHERE "Suburb_Id" IS NOT NULL
	GROUP BY "SUBURB", "Suburb_Id"
	ORDER BY v;
	

INSERT INTO asset.ref_district
SELECT 
	DISTINCT(code) AS k, names.name AS v
FROM
	(SELECT "DISTRICT" AS name FROM asset_import) names
	JOIN
	(SELECT LEFT(REPLACE("DISTRICT", ' ', ''),10) AS code, "DISTRICT" as name FROM asset_import) codes
	ON names.name = codes.name;


INSERT INTO asset.ref_municipality
SELECT 
	DISTINCT(code) AS k, names.name AS v
FROM
	(SELECT "LOCAL" AS name FROM asset_import) names
	JOIN
	(SELECT REGEXP_REPLACE(LEFT(REPLACE("LOCAL", ' ', ''),10),'[^[:alnum:]]','', 'g') AS code, "LOCAL" as name FROM asset_import) codes
	ON names.name = codes.name;
	

UPDATE asset_import SET "TOWN" = 'VANRHYNSDORP' WHERE "TOWN" = 'VAN RHYNSDORP';

INSERT INTO asset.ref_town
SELECT 
	DISTINCT(code) AS k, names.name AS v
FROM
	(SELECT "TOWN" AS name FROM asset_import) names
	JOIN
	(SELECT REGEXP_REPLACE(LEFT(REPLACE("TOWN", ' ', ''),9)||RIGHT(REPLACE("TOWN", ' ', ''),1), '[^[:alnum:]]', '','g') AS code, "TOWN" as name FROM asset_import) codes
	ON names.name = codes.name
ORDER BY names.name ASC;
	
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
	(SELECT (asset_id) FROM asset WHERE code = "Code (uk)") AS asset_id,
	"AssetTypeID" AS facility_type_code
FROM asset_import WHERE "AssetTypeID" IS NOT NULL;
