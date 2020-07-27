DROP VIEW dtpw.dtpw_export_view;

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
