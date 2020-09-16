DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view CASCADE;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_ei_view CASCADE;

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
AS SELECT
       '1_3' AS obj_version,
       core.asset_id,
       core.asset_type_code,
       core.name,
       core.description,
       core.func_loc_path,
       core.active,
       core.latitude,
       core.longitude,
       core.address,
       core.geom,
       core.barcode,
       core.serial_number,
       core.municipality_code,
       rm.v AS municipality_value,
       core.town_code,
       rt.v AS town_value,
       core.suburb_code,
       rs.v AS suburb_value,
       core.district_code,
       rd.v  AS district_value,
       a_tp_f.facility_type_code,
       classification.responsible_dept_code,
       classification.is_owned
   FROM asset_core_view core
            LEFT JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
            LEFT JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
            LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id
            LEFT JOIN public.ref_district rd ON rd.k = core.district_code
            LEFT JOIN public.ref_municipality rm ON rm.k = core.municipality_code
            LEFT JOIN public.ref_town rt ON rt.k = core.town_code
            LEFT JOIN public.ref_suburb rs ON rs.k = core.suburb_code
            LEFT JOIN asset_classification classification ON core.asset_id = classification.asset_id
;
COMMENT ON VIEW dtpw.asset_core_dtpw_view IS 'DTPW view. Joins facility and envelope information onto core information.';

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi AS
SELECT a.*, p.lpi
FROM
    dtpw.asset_core_dtpw_view a
        JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
        JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;
COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';



--
-- Department specific
--
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_ei_view AS
SELECT
    core.*,
    public.asset_grouping.grouping_id AS "EMIS",
    dtpw.ei_district_link.k_education_district as ei_district_code,
    dtpw.ref_ei_district.v as ei_district_value
FROM
    dtpw.asset_core_dtpw_view core
        LEFT JOIN asset_grouping ON core.asset_id = asset_grouping.asset_id
        AND asset_grouping.grouping_id_type = (( SELECT grouping_id_type.type_id
                                                 FROM grouping_id_type
                                                 WHERE grouping_id_type.name::text = 'EMIS'::text))
        LEFT JOIN dtpw.ei_district_link ON core.asset_id = dtpw.ei_district_link.asset_id
        LEFT JOIN dtpw.ref_ei_district ON k_education_district = dtpw.ref_ei_district.k
;



--
-- Materialised views
--

CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view
AS
SELECT '1_3'::text AS obj_version,
       dtpw.asset_core_dtpw_view.asset_id,
       dtpw.asset_core_dtpw_view.asset_type_code,
       dtpw.asset_core_dtpw_view.name,
       dtpw.asset_core_dtpw_view.func_loc_path::text AS func_loc_path,
       dtpw.asset_core_dtpw_view.active,
       dtpw.asset_core_dtpw_view.latitude,
       dtpw.asset_core_dtpw_view.longitude,
       dtpw.asset_core_dtpw_view.address,
       dtpw.asset_core_dtpw_view.geom,
       dtpw.asset_core_dtpw_view.barcode,
       dtpw.asset_core_dtpw_view.serial_number,
       dtpw.asset_core_dtpw_view.district_code,
       dtpw.asset_core_dtpw_view.district_value,
       dtpw.asset_core_dtpw_view.municipality_code,
       dtpw.asset_core_dtpw_view.municipality_value,
       dtpw.asset_core_dtpw_view.town_code,
       dtpw.asset_core_dtpw_view.town_value,
       dtpw.asset_core_dtpw_view.suburb_code,
       dtpw.asset_core_dtpw_view.suburb_value,
       dtpw.asset_core_dtpw_view.facility_type_code,
       dtpw.asset_core_dtpw_view.responsible_dept_code,
       dtpw.asset_core_dtpw_view.is_owned
FROM dtpw.asset_core_dtpw_view
WITH DATA;

CREATE UNIQUE INDEX m1_asset_id_idx ON dtpw.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON dtpw.dtpw_core_report_view USING btree (func_loc_path);
CREATE INDEX m1_geom_idx  ON dtpw.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON dtpw.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON dtpw.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON dtpw.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON dtpw.dtpw_core_report_view USING btree (town_code);
CREATE INDEX m1_responsible_dept_code_idx ON dtpw.dtpw_core_report_view USING btree (responsible_dept_code);
CREATE INDEX m1_is_owned_idx ON dtpw.dtpw_core_report_view USING btree (is_owned);

COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';
REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;

CREATE OR REPLACE VIEW dtpw.dtpw_core_report_view_wrapper
AS
SELECT dtpw.dtpw_core_report_view.obj_version,
       dtpw.dtpw_core_report_view.asset_id,
       dtpw.dtpw_core_report_view.asset_type_code,
       dtpw.dtpw_core_report_view.name,
       dtpw.dtpw_core_report_view.func_loc_path,
       dtpw.dtpw_core_report_view.active,
       dtpw.dtpw_core_report_view.latitude,
       dtpw.dtpw_core_report_view.longitude,
       dtpw.dtpw_core_report_view.address,
       dtpw.dtpw_core_report_view.geom,
       dtpw.dtpw_core_report_view.barcode,
       dtpw.dtpw_core_report_view.serial_number,
       dtpw.dtpw_core_report_view.district_code,
       dtpw.dtpw_core_report_view.district_value,
       dtpw.dtpw_core_report_view.municipality_code,
       dtpw.dtpw_core_report_view.municipality_value,
       dtpw.dtpw_core_report_view.town_code,
       dtpw.dtpw_core_report_view.town_value,
       dtpw.dtpw_core_report_view.suburb_code,
       dtpw.dtpw_core_report_view.suburb_value,
       dtpw.dtpw_core_report_view.facility_type_code,
       dtpw.dtpw_core_report_view.responsible_dept_code,
       dtpw.dtpw_core_report_view.is_owned
FROM dtpw.dtpw_core_report_view;


-- EI
CREATE MATERIALIZED VIEW dtpw.dtpw_ei_report_view
AS
SELECT '1_0'::text AS obj_version,
       dtpw.asset_core_dtpw_ei_view.asset_id,
       dtpw.asset_core_dtpw_ei_view.asset_type_code,
       dtpw.asset_core_dtpw_ei_view.name,
       dtpw.asset_core_dtpw_ei_view.func_loc_path::text AS func_loc_path,
       dtpw.asset_core_dtpw_ei_view.active,
       dtpw.asset_core_dtpw_ei_view.latitude,
       dtpw.asset_core_dtpw_ei_view.longitude,
       dtpw.asset_core_dtpw_ei_view.address,
       dtpw.asset_core_dtpw_ei_view.geom,
       dtpw.asset_core_dtpw_ei_view.barcode,
       dtpw.asset_core_dtpw_ei_view.serial_number,
       dtpw.asset_core_dtpw_ei_view.district_code,
       dtpw.asset_core_dtpw_ei_view.district_value,
       dtpw.asset_core_dtpw_ei_view.municipality_code,
       dtpw.asset_core_dtpw_ei_view.municipality_value,
       dtpw.asset_core_dtpw_ei_view.town_code,
       dtpw.asset_core_dtpw_ei_view.town_value,
       dtpw.asset_core_dtpw_ei_view.suburb_code,
       dtpw.asset_core_dtpw_ei_view.suburb_value,
       dtpw.asset_core_dtpw_ei_view.facility_type_code,
       dtpw.asset_core_dtpw_ei_view.responsible_dept_code,
       dtpw.asset_core_dtpw_ei_view.is_owned,
       dtpw.asset_core_dtpw_ei_view."EMIS",
       dtpw.asset_core_dtpw_ei_view.ei_district_code,
       dtpw.asset_core_dtpw_ei_view.ei_district_value
FROM dtpw.asset_core_dtpw_ei_view
WITH DATA;

CREATE UNIQUE INDEX m2_asset_id_idx ON dtpw.dtpw_ei_report_view USING btree (asset_id);
CREATE INDEX m2_func_loc_path_idx  ON dtpw.dtpw_ei_report_view USING btree (func_loc_path);
CREATE INDEX m2_geom_idx  ON dtpw.dtpw_ei_report_view USING gist (geom);
CREATE INDEX m2_district_code_idx ON dtpw.dtpw_ei_report_view USING btree (district_code);
CREATE INDEX m2_municipality_code_idx ON dtpw.dtpw_ei_report_view USING btree (municipality_code);
CREATE INDEX m2_suburb_code_idx ON dtpw.dtpw_ei_report_view USING btree (suburb_code);
CREATE INDEX m2_town_code_idx ON dtpw.dtpw_ei_report_view USING btree (town_code);
CREATE INDEX "m2_EMIS_idx" ON dtpw.dtpw_ei_report_view USING btree ("EMIS");
CREATE INDEX m2_responsible_dept_code_idx ON dtpw.dtpw_ei_report_view USING btree (responsible_dept_code);
CREATE INDEX m2_is_owned_idx ON dtpw.dtpw_ei_report_view USING btree (is_owned);

REFRESH MATERIALIZED VIEW dtpw.dtpw_ei_report_view;

CREATE OR REPLACE VIEW dtpw.dtpw_ei_report_view_wrapper
AS
SELECT dtpw.dtpw_ei_report_view.obj_version,
       dtpw.dtpw_ei_report_view.asset_id,
       dtpw.dtpw_ei_report_view.asset_type_code,
       dtpw.dtpw_ei_report_view.name,
       dtpw.dtpw_ei_report_view.func_loc_path,
       dtpw.dtpw_ei_report_view.active,
       dtpw.dtpw_ei_report_view.latitude,
       dtpw.dtpw_ei_report_view.longitude,
       dtpw.dtpw_ei_report_view.address,
       dtpw.dtpw_ei_report_view.geom,
       dtpw.dtpw_ei_report_view.barcode,
       dtpw.dtpw_ei_report_view.serial_number,
       dtpw.dtpw_ei_report_view.district_code,
       dtpw.dtpw_ei_report_view.district_value,
       dtpw.dtpw_ei_report_view.municipality_code,
       dtpw.dtpw_ei_report_view.municipality_value,
       dtpw.dtpw_ei_report_view.town_code,
       dtpw.dtpw_ei_report_view.town_value,
       dtpw.dtpw_ei_report_view.suburb_code,
       dtpw.dtpw_ei_report_view.suburb_value,
       dtpw.dtpw_ei_report_view.facility_type_code,
       dtpw.dtpw_ei_report_view.responsible_dept_code,
       dtpw.dtpw_ei_report_view.is_owned,

       dtpw.dtpw_ei_report_view."EMIS",
       dtpw.dtpw_ei_report_view.ei_district_code,
       dtpw.dtpw_ei_report_view.ei_district_value
FROM dtpw.dtpw_ei_report_view;





--
-- Export view
--
CREATE OR REPLACE VIEW dtpw.dtpw_export_view AS
SELECT
    core.asset_id,
    core.asset_type_code,
    core.name,
    replace(ltree2text(core.func_loc_path),'.', '-') AS func_loc_path,
    core.active,
    core.latitude,
    core.longitude,
    core.address,
    core.barcode,
    core.serial_number,
    core.district_code,
    core.municipality_code,
    core.town_code,
    core.suburb_code,
    core.facility_type_code,
    core.responsible_dept_code,
    core.is_owned,

    ei."EMIS",
    ei.ei_district_code,

    ST_asText(core.geom) AS geom
FROM dtpw.asset_core_dtpw_ei_view core LEFT JOIN dtpw.asset_core_dtpw_ei_view ei ON core.asset_id = ei.asset_id;

COMMENT ON VIEW dtpw.dtpw_export_view IS 'Converts the geometry to well-known text and provides all asset rows';

