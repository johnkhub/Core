CREATE OR REPLACE VIEW public.asset_core_dtpw_view AS
SELECT 
	core.*, 
	a_tp_e.district_code,
    a_tp_e.municipality_code,
    a_tp_e.town_code,
    a_tp_e.suburb_code,
	
	a_tp_f.facility_type_code,
	
	responsible_dept_code, 
	asset_link.external_id AS "EMIS"
FROM 
	asset_core_view core 
	JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
	LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id

	JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
	LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id

	JOIN asset_classification classification ON core.asset_id = classification.asset_id
	LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id AND asset_link.external_id_type = (SELECT type_id FROM external_id_type WHERE name = 'EMIS');
	
COMMENT ON VIEW public.asset_core_dtpw_view IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';

	
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


CREATE OR REPLACE VIEW landparcel_view AS
SELECT 
	a.*, p.lpi
FROM asset a JOIN asset.a_tp_landparcel p ON a.asset_id = p.asset_id;

CREATE OR REPLACE VIEW asset_core_dtpw_view_with_lpi AS
SELECT a.*, p.lpi 
FROM 
	public.asset_core_dtpw_view a 
	JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
	JOIN public.landparcel_view p ON l.landparcel_asset_id = p.asset_id;
COMMENT ON VIEW public.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';