DROP VIEW IF EXISTS public.import_report_view;
--
-- Import report
--
CREATE OR REPLACE VIEW dtpw.import_report_view AS
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

COMMENT ON VIEW dtpw.import_report_view IS 'A view that shows the number of entities of each type that was IMPORTED as well as indication of how many attribute values are missing.';
