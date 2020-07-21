DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view CASCADE;

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
AS SELECT
       '1_2' AS obj_version,
       core.asset_id,
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
       a_tp_e.municipality_code,
       rm.v AS municipality_value,
       a_tp_e.town_code,
       rt.v AS town_value,
       a_tp_e.suburb_code,
       rs.v AS suburb_value,
       a_tp_e.district_code,
       rd.v  AS district_value,
       a_tp_f.facility_type_code,
       classification.responsible_dept_code,
       classification.is_owned,
       asset_link.external_id AS "EMIS"
   FROM asset_core_view core
            LEFT JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
            LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id
            LEFT JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
            LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id
            LEFT JOIN asset.ref_district rd ON rd.k = a_tp_e.district_code
            LEFT JOIN asset.ref_municipality rm ON rm.k = a_tp_e.municipality_code
            LEFT JOIN asset.ref_town rt ON rt.k = a_tp_e.town_code
            LEFT JOIN asset.ref_suburb rs ON rs.k = a_tp_e.suburb_code
            LEFT JOIN asset_classification classification ON core.asset_id = classification.asset_id
            LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id
       AND asset_link.external_id_type = (( SELECT external_id_type.type_id
                                            FROM external_id_type
                                            WHERE external_id_type.name::text = 'EMIS'::text));

COMMENT ON VIEW dtpw.asset_core_dtpw_view IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi AS
SELECT a.*, p.lpi
FROM
    dtpw.asset_core_dtpw_view a
        JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
        JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;
COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';


CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view
AS
SELECT '1_2'::text AS obj_version,
       asset_core_dtpw_view.asset_id,
       asset_core_dtpw_view.asset_type_code,
       asset_core_dtpw_view.name,
       asset_core_dtpw_view.func_loc_path::text AS func_loc_path,
       asset_core_dtpw_view.active,
       asset_core_dtpw_view.latitude,
       asset_core_dtpw_view.longitude,
       asset_core_dtpw_view.address,
       asset_core_dtpw_view.geom,
       asset_core_dtpw_view.barcode,
       asset_core_dtpw_view.serial_number,
       asset_core_dtpw_view.district_code,
       asset_core_dtpw_view.district_value,
       asset_core_dtpw_view.municipality_code,
       asset_core_dtpw_view.municipality_value,
       asset_core_dtpw_view.town_code,
       asset_core_dtpw_view.town_value,
       asset_core_dtpw_view.suburb_code,
       asset_core_dtpw_view.suburb_value,
       asset_core_dtpw_view.facility_type_code,
       asset_core_dtpw_view.responsible_dept_code,
       asset_core_dtpw_view.is_owned,
       asset_core_dtpw_view."EMIS"
FROM dtpw.asset_core_dtpw_view
WITH DATA;

COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view
    IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';



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

CREATE OR REPLACE VIEW dtpw.dtpw_export_view AS
SELECT
    asset_core_dtpw_view.asset_id,
    asset_core_dtpw_view.asset_type_code,
    asset_core_dtpw_view.name,
    replace(ltree2text(asset_core_dtpw_view.func_loc_path),'.', '-') AS func_loc_path,
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
    ST_asText(asset_core_dtpw_view.geom) AS geom
FROM dtpw.asset_core_dtpw_view;

COMMENT ON VIEW dtpw.dtpw_export_view IS 'This is an exact copy of the select in dtpw.dtpw_core_report_view but it converts the geometry to well-known text.';


-- View: dtpw.dtpw_core_report_view_wrapper

-- DROP VIEW dtpw.dtpw_core_report_view_wrapper;

CREATE OR REPLACE VIEW dtpw.dtpw_core_report_view_wrapper
AS
SELECT dtpw_core_report_view.obj_version,
       dtpw_core_report_view.asset_id,
       dtpw_core_report_view.asset_type_code,
       dtpw_core_report_view.name,
       dtpw_core_report_view.func_loc_path,
       dtpw_core_report_view.active,
       dtpw_core_report_view.latitude,
       dtpw_core_report_view.longitude,
       dtpw_core_report_view.address,
       dtpw_core_report_view.geom,
       dtpw_core_report_view.barcode,
       dtpw_core_report_view.serial_number,
       dtpw_core_report_view.district_code,
       dtpw_core_report_view.district_value,
       dtpw_core_report_view.municipality_code,
       dtpw_core_report_view.municipality_value,
       dtpw_core_report_view.town_code,
       dtpw_core_report_view.town_value,
       dtpw_core_report_view.suburb_code,
       dtpw_core_report_view.suburb_value,
       dtpw_core_report_view.facility_type_code,
       dtpw_core_report_view.responsible_dept_code,
       dtpw_core_report_view.is_owned,
       dtpw_core_report_view."EMIS"
FROM dtpw.dtpw_core_report_view;

--GRANT SELECT ON TABLE dtpw.dtpw_core_report_view_wrapper TO powerbi;
