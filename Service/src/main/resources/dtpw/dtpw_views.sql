DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view CASCADE;

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
        LEFT JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
        LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id

        LEFT JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
        LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id

        LEFT JOIN asset_classification classification ON core.asset_id = classification.asset_id
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

--SELECT * FROM public.index_definition_by_view_view

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

REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;