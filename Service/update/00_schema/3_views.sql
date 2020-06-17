CREATE OR REPLACE VIEW public.asset_core_view
AS
SELECT a.asset_id,
       a.asset_type_code,
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


-- View: dtpw.asset_core_dtpw_view
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
AS
SELECT core.asset_id,
       core.asset_type_code,
       core.name,
       core.func_loc_path,
       core.active,
       core.latitude,
       core.longitude,
       core.address,
       core.geom,
       core.barcode,
       core.serial_number,
       a_tp_e.district_code,
       a_tp_e.municipality_code,
       a_tp_e.town_code,
       a_tp_e.suburb_code,
       a_tp_f.facility_type_code,
       classification.responsible_dept_code,
       classification.is_owned,
       asset_link.external_id AS "EMIS"
FROM asset_core_view core
         LEFT JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
         LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id
         LEFT JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
         LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id
         LEFT JOIN asset_classification classification ON core.asset_id = classification.asset_id
         LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id AND asset_link.external_id_type = (( SELECT external_id_type.type_id
                                                                                                          FROM external_id_type
                                                                                                          WHERE external_id_type.name::text = 'EMIS'::text));


COMMENT ON VIEW dtpw.asset_core_dtpw_view
    IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';

-- View: dtpw.asset_core_dtpw_view_with_lpi
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi
AS
SELECT a.asset_id,
       a.asset_type_code,
       a.name,
       a.func_loc_path,
       a.active,
       a.latitude,
       a.longitude,
       a.address,
       a.geom,
       a.barcode,
       a.serial_number,
       a.district_code,
       a.municipality_code,
       a.town_code,
       a.suburb_code,
       a.facility_type_code,
       a.responsible_dept_code,
       a.is_owned,
       a."EMIS",
       p.lpi
FROM dtpw.asset_core_dtpw_view a
         JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
         JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;

COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';

CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view AS
SELECT asset_core_dtpw_view.asset_id,
       asset_core_dtpw_view.asset_type_code,
       asset_core_dtpw_view.name,
       asset_core_dtpw_view.func_loc_path::text,
       asset_core_dtpw_view.active,
       asset_core_dtpw_view.latitude,
       asset_core_dtpw_view.longitude,
       asset_core_dtpw_view.address,
       asset_core_dtpw_view.barcode,
       asset_core_dtpw_view.serial_number,
       asset_core_dtpw_view.district_code,
       asset_core_dtpw_view.municipality_code,
       asset_core_dtpw_view.town_code,
       asset_core_dtpw_view.suburb_code,
       asset_core_dtpw_view.facility_type_code,
       asset_core_dtpw_view.responsible_dept_code,
       asset_core_dtpw_view.is_owned,
       asset_core_dtpw_view."EMIS",
       asset_core_dtpw_view.geom
FROM dtpw.asset_core_dtpw_view
WITH DATA;


CREATE UNIQUE INDEX m1_asset_id_idx ON dtpw.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON dtpw.dtpw_core_report_view USING btree (func_loc_path);
CREATE INDEX m1_geom_idx  ON dtpw.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON dtpw.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON dtpw.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON dtpw.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON dtpw.dtpw_core_report_view USING btree (town_code);
CREATE INDEX "m1_EMIS_idx" ON dtpw.dtpw_core_report_view USING btree ("EMIS");
CREATE INDEX m1_responsible_dept_code_idx ON dtpw.dtpw_core_report_view USING btree (responsible_dept_code);
CREATE INDEX m1_is_owned_idx ON dtpw.dtpw_core_report_view USING btree (is_owned);


COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';

REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;


CREATE OR REPLACE VIEW public.import_report_view
AS SELECT totals."Asset Type",
          totals."Total",
          location."Num with no location (lat/long)",
          geoms."Num with no geometry",
          classification."Num with no Responsible Department",
          location_address."Num with no address",
          identification_barcode."Num with no barcode",
          identification_serial_number."Num with no serial number"
   FROM ( SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'ENVELOPE'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'FACILITY'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'BUILDING'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'SITE'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'FLOOR'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'ROOM'::text
          GROUP BY asset.asset_type_code
          UNION
          SELECT asset.asset_type_code AS "Asset Type",
                 count(asset.asset_id) AS "Total"
          FROM asset
          WHERE asset.asset_type_code::text = 'LANDPARCEL'::text
          GROUP BY asset.asset_type_code) totals
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no location (lat/long)"
                   FROM asset a
                            LEFT JOIN location l ON a.asset_id = l.asset_id
                   WHERE l.asset_id IS NULL AND l.latitude IS NULL OR l.longitude IS NULL
                   GROUP BY a.asset_type_code) location ON totals."Asset Type"::text = location."Asset Type"::text
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no address"
                   FROM asset a
                            LEFT JOIN location l ON a.asset_id = l.asset_id
                   WHERE l.asset_id IS NULL OR l.address IS NULL
                   GROUP BY a.asset_type_code) location_address ON totals."Asset Type"::text = location_address."Asset Type"::text
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no geometry"
                   FROM asset a
                            LEFT JOIN geoms g ON a.asset_id = g.asset_id
                   WHERE g.asset_id IS NULL
                   GROUP BY a.asset_type_code) geoms ON totals."Asset Type"::text = geoms."Asset Type"::text
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no Responsible Department"
                   FROM asset a
                            LEFT JOIN asset_classification c ON a.asset_id = c.asset_id
                   WHERE c.asset_id IS NULL OR c.responsible_dept_code IS NULL
                   GROUP BY a.asset_type_code) classification ON totals."Asset Type"::text = classification."Asset Type"::text
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no barcode"
                   FROM asset a
                            LEFT JOIN asset_identification i ON a.asset_id = i.asset_id
                   WHERE i.asset_id IS NULL OR i.barcode IS NULL
                   GROUP BY a.asset_type_code) identification_barcode ON totals."Asset Type"::text = identification_barcode."Asset Type"::text
            JOIN ( SELECT a.asset_type_code AS "Asset Type",
                          count(a.asset_id) AS "Num with no serial number"
                   FROM asset a
                            LEFT JOIN asset_identification i ON a.asset_id = i.asset_id
                   WHERE i.asset_id IS NULL OR i.serial_number IS NULL
                   GROUP BY a.asset_type_code) identification_serial_number ON totals."Asset Type"::text = identification_serial_number."Asset Type"::text
   ORDER BY totals."Asset Type";

comment on view public.import_report_view is 'A view that shows the number of entities of each type that was IMPORTED as well as indication of how many attribute values are missing.';
