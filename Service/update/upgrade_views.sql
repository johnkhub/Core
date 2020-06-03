CREATE OR REPLACE VIEW public.asset_core_view AS
SELECT a.asset_id,
       a.asset_type_code AS asset_type,
       a.name,
       a.func_loc_path,
       a.deactivated_at IS NULL AS active,

       location.latitude,
       location.longitude,
       location.address,

       geoms.geom,

       identification.barcode,
       identification.serial_number

FROM asset a
         LEFT JOIN location ON a.asset_id = location.asset_id
         LEFT JOIN geoms ON a.asset_id = geoms.asset_id
         LEFT JOIN asset_identification identification ON a.asset_id = identification.asset_id;

COMMENT ON VIEW public.asset_core_view IS 'Inner join the basic core tables indicating what it is and where it is';



CREATE OR REPLACE VIEW asset.landparcel_view AS
SELECT
    a.*, p.lpi
FROM asset a JOIN asset.a_tp_landparcel p ON a.asset_id = p.asset_id;


CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view AS
SELECT
    core.*,
    a_tp_e.district_code,
    a_tp_e.municipality_code,
    a_tp_e.town_code,
    a_tp_e.suburb_code,

    a_tp_f.facility_type_code,

    classification.responsible_dept_code,
    classification.is_owned,
    asset_link.external_id AS "EMIS"
FROM
    public.asset_core_view core
        JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
        LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id

        JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
        LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id

        JOIN asset_classification classification ON core.asset_id = classification.asset_id
        LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id AND asset_link.external_id_type = (SELECT type_id FROM external_id_type WHERE name = 'EMIS');

COMMENT ON VIEW dtpw.asset_core_dtpw_view IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi AS
SELECT a.*, p.lpi
FROM
    dtpw.asset_core_dtpw_view a
        JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
        JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;
COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';

CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view AS SELECT * FROM dtpw.asset_core_dtpw_view;

CREATE UNIQUE INDEX m1_asset_id_idx ON dtpw.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON dtpw.dtpw_core_report_view USING gist (func_loc_path);
CREATE INDEX m1_geom_idx  ON dtpw.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON dtpw.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON dtpw.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON dtpw.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON dtpw.dtpw_core_report_view USING btree (town_code);
CREATE INDEX "m1_EMIS_idx" ON dtpw.dtpw_core_report_view USING btree ("EMIS");
CREATE INDEX m1_responsible_dept_code_idx ON dtpw.dtpw_core_report_view USING btree (responsible_dept_code);
CREATE INDEX m1_is_owned_idx ON dtpw.dtpw_core_report_view USING btree (is_owned);


COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';

--REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;

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
