CREATE OR REPLACE VIEW dtpw.dtpw_export_view AS
SELECT
   asset_core_dtpw_view.asset_id,
   asset_core_dtpw_view.asset_type_code,
   asset_core_dtpw_view.name,
   asset_core_dtpw_view.func_loc_path,
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


INSERT INTO public.modtrack_meta (recid, version, identity) VALUES (1,1, 'f6bdbb73-bc0a-4181-806d-dede7b02fb0f');
INSERT INTO public.modtrack_tables (recid, tablename, createcount, stamp) VALUES (1,'asset', 1, 1);



