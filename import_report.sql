--
-- Import report
--
CREATE OR REPLACE VIEW import_report_view AS
SELECT 
	totals."Asset Type",
	"Total",
	"Num with no location (lat/long)",
	"Num with no geometry",
	"Num with no Responsible Department",
	"Num with no address",
	"Num with no barcode",
	"Num with no serial number"
FROM
(
	SELECT asset_type_code AS "Asset Type", count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'ENVELOPE' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type", count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'FACILITY' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type", count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'BUILDING' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type", count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'SITE' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type",count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'FLOOR' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type",count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'ROOM' GROUP BY asset_type_code
	UNION
	SELECT asset_type_code AS "Asset Type",count(asset_id) AS "Total" FROM asset WHERE asset_type_code = 'LANDPARCEL' GROUP BY asset_type_code
) AS totals
JOIN
(
	-- location
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no location (lat/long)"
	FROM 
		asset AS a LEFT JOIN 
		location as l  ON a.asset_id = l.asset_id
	WHERE l.asset_id IS NULL AND l.latitude IS NULL OR l.longitude IS NULL
	GROUP BY a.asset_type_code
) AS location 
ON totals."Asset Type" = location."Asset Type"
JOIN
(
	-- location
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no address"
	FROM 
		asset AS a LEFT JOIN 
		location as l ON a.asset_id = l.asset_id
	WHERE l.asset_id IS NULL OR l.address IS NULL
	GROUP BY a.asset_type_code
) AS location_address
ON totals."Asset Type" = location_address."Asset Type"
JOIN
(
	-- geoms
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no geometry"
	FROM 
		asset AS a LEFT JOIN 
		geoms as g  ON a.asset_id = g.asset_id
	WHERE g.asset_id IS NULL
	GROUP BY a.asset_type_code
) AS geoms
ON totals."Asset Type" = geoms."Asset Type"
JOIN
(
	-- asset_classification
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no Responsible Department"
	FROM 
		asset AS a LEFT JOIN 
		asset_classification as c  ON a.asset_id = c.asset_id
	WHERE c.asset_id IS NULL OR c.responsible_dept_code IS NULL
	GROUP BY a.asset_type_code
) AS classification
ON totals."Asset Type" = classification."Asset Type"
JOIN
(
	-- asset_identification
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no barcode"
	FROM 
		asset AS a LEFT JOIN 
		asset_identification as i  ON a.asset_id = i.asset_id
	WHERE i.asset_id IS NULL OR i.barcode IS NULL
	GROUP BY a.asset_type_code
) AS identification_barcode
ON totals."Asset Type" = identification_barcode."Asset Type"
JOIN
(
	-- asset_identification
	SELECT 
		a.asset_type_code "Asset Type", count(a.asset_id) "Num with no serial number"
	FROM 
		asset AS a LEFT JOIN 
		asset_identification as i  ON a.asset_id = i.asset_id
	WHERE i.asset_id IS NULL OR i.serial_number IS NULL
	GROUP BY a.asset_type_code
) AS identification_serial_number
ON totals."Asset Type" = identification_serial_number."Asset Type"
ORDER BY  totals."Asset Type"; 

COMMENT ON VIEW import_report_view IS 'A view that shows the number of entities of each type that was IMPORTED as well as indication of how many attribute values are missing.';

CREATE OR REPLACE VIEW source_report_view AS
SELECT 
    CASE 
		WHEN dept."Level" = 'Asset' THEN 'Envelope'
		ELSE
			dept."Level"	
	END, 
    latlong."Num with no location (lat/long)",
    geom."Num with no geometry",
    dept."Num with no Responsible Department",
    address."Num with no address",
	barcode."Num with no barcode",
	serial_number."Num with no serial number"
FROM
(
    SELECT "Level", count("AssetID") "Num with no Responsible Department"
    FROM asset_import 
    WHERE  "Department" IS NULL
    GROUP BY "Level"
    HAVING count("AssetID") > 0
)  dept
JOIN 
(
    SELECT "Level", count("AssetID") "Num with no address"
    FROM asset_import 
    WHERE  "LocationAddress" IS NULL
    GROUP BY "Level"
    HAVING count("AssetID") > 0
) address
ON dept."Level" = address."Level"
JOIN
(
    SELECT "Level", count("AssetID") "Num with no location (lat/long)"
    FROM asset_import 
    WHERE  "X" IS NULL AND "Y" IS NULL
    GROUP BY "Level"
    HAVING count("AssetID") > 0
) latlong
ON dept."Level" = latlong."Level"
JOIN
(
    SELECT "Level", count("AssetID") "Num with no geometry"
    FROM asset_import 
    WHERE  "Geometry" IS NULL OR "Geometry" = '#N/A'
    GROUP BY "Level"
    HAVING count("AssetID") > 0
) geom
ON dept."Level" = geom."Level"
JOIN
(
    SELECT "Level", count("AssetID") "Num with no barcode"
    FROM asset_import 
    WHERE  "Barcode" IS NULL
    GROUP BY "Level"
    HAVING count("AssetID") > 0
) barcode
ON dept."Level" = barcode."Level"
JOIN
(
    SELECT "Level", count("AssetID") "Num with no serial number"
    FROM asset_import 
    WHERE  "Serial_Number" IS NULL
    GROUP BY "Level"
    HAVING count("AssetID") > 0
) serial_number
ON dept."Level" = serial_number."Level"
ORDER BY "Level";
COMMENT ON VIEW source_report_view IS 'A view that shows the number of entities of each type that was the SOURCE of an import as well as indication of how many attribute values are missing.';


-- TODO: Must add Landparcels to source_report_view. Must add serial and barcode to import source data
-- TODO: Dept won't match at the moment as when we import we propagate the dept to all children while this not the case on the source data
SELECT 
	import_report_view."Asset Type",
	import_report_view."Num with no location (lat/long)" - source_report_view."Num with no location (lat/long)" AS "Recon: Num with no location (lat/long)",
	import_report_view."Num with no geometry" - source_report_view. "Num with no geometry"  AS "Recon: Num with no geometry" ,
	import_report_view."Num with no Responsible Department" - source_report_view."Num with no Responsible Department" AS "Recon: Num with no Responsible Department",
	import_report_view."Num with no address" - source_report_view."Num with no address" AS "Recon: Num with no address",
	import_report_view."Num with no barcode" - source_report_view."Num with no barcode" AS "Recon: Num with no barcode",
	import_report_view."Num with no serial number" - source_report_view."Num with no serial number"  AS "Recon: Num with no serial number"  
FROM  
	import_report_view 
	LEFT JOIN source_report_view 
	ON UPPER(source_report_view."Level") = UPPER(import_report_view."Asset Type");


SELECT * FROM import_report_view;

SELECT count(a.asset_id) "Envelopes with partial/missing mun,town,site or suburb"
FROM 
	asset AS a JOIN asset.a_tp_envelope e  ON a.asset_id = e.asset_id
WHERE a.asset_type_code = 'ENVELOPE' AND (e.municipality_code IS NULL OR e.town_code IS NULL OR e.district_code IS NULL)
GROUP BY a.asset_type_code;


SELECT count(a.asset_id) "Facilities with no facility code"
FROM 
	asset AS a LEFT JOIN asset.a_tp_facility f  ON a.asset_id = f.asset_id
WHERE a.asset_type_code = 'FACILITY' AND (f.asset_id IS NULL OR f.facility_type_code IS NULL);

