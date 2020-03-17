CREATE MATERIALIZED VIEW public.dtpw_core_report_view AS SELECT * FROM asset_core_dtpw_view;

--SELECT * FROM public.index_definition_by_view_view 

CREATE UNIQUE INDEX m1_asset_id_idx ON public.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON public.dtpw_core_report_view USING gist (func_loc_path);
CREATE INDEX m1_geom_idx  ON public.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON public.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON public.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON public.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON public.dtpw_core_report_view USING btree (town_code);
CREATE INDEX "m1_EMIS_idx" ON public.dtpw_core_report_view USING btree ("EMIS");
CREATE INDEX m1_responsible_dept_code_idx ON public.dtpw_core_report_view USING btree (responsible_dept_code);

COMMENT ON MATERIALIZED VIEW public.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';

REFRESH MATERIALIZED VIEW dtpw_core_report_view;