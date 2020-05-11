--
-- Given the table `asset_import`, populates
--
--      asset.ref_suburb
--      asset.ref_district
--      asset.ref_municipality
--      asset.ref_town
--
-- NOTE: This clears out and repopulates the tables  

DELETE FROM asset.ref_suburb;
DELETE FROM asset.ref_district;
DELETE FROM asset.ref_municipality;
DELETE FROM asset.ref_town;

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