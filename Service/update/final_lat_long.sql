
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view_with_lpi;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view;
DROP VIEW IF EXISTS public.asset_core_view;
DROP VIEW IF EXISTS public.import_report_view;

alter table public.location alter column latitude type numeric(11,8);
alter table public.location alter column longitude type numeric(11,8);

CREATE OR REPLACE VIEW public.asset_core_view
 AS
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


-- View: dtpw.asset_core_dtpw_view
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
 AS
 SELECT core.asset_id,
    core.asset_type,
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
    a.asset_type,
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








