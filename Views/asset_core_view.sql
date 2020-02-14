CREATE OR REPLACE VIEW asset_core_view AS
SELECT 
	a.asset_id AS asset_id, 
	a.asset_type_code AS asset_type, 
	a.name AS name, 
	a.func_loc_path, 
	district_code, municipality_code, town_code, suburb_code,
	latitude, longitude,
	geom
FROM 
	asset a	
	-- Get the envelope and join that to the location of the envelope and the geom of the envelope
	JOIN asset e ON subpath(a.func_loc_path,0,1) = e.func_loc_path 
	LEFT JOIN location ON e.asset_id = location.asset_id
	LEFT JOIN geoms on e.asset_id = geoms.asset_id
	LEFT JOIN asset.a_tp_envelope a_tp_e on e.asset_id = a_tp_e.asset_id;
COMMENT ON VIEW asset_core_view IS 'Basic view on core assets. Simplistically *what* it is and *where* it is.';
