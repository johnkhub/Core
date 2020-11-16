CREATE UNIQUE INDEX m1_asset_id_idx ON dtpw.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON dtpw.dtpw_core_report_view USING btree (func_loc_path);
CREATE INDEX m1_geom_idx  ON dtpw.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON dtpw.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON dtpw.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON dtpw.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON dtpw.dtpw_core_report_view USING btree (town_code);
-- DTPW specific
CREATE INDEX m1_responsible_dept_code_idx ON dtpw.dtpw_core_report_view USING btree (responsible_dept_code);
CREATE INDEX m1_is_owned_idx ON dtpw.dtpw_core_report_view USING btree (is_owned);

COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';
REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;

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