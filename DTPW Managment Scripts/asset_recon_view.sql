SELECT a.asset_id,
	CASE a.asset_type_code 
	WHEN 'ENVELOPE' THEN 'Asset'
	WHEN 'FACILITY' THEN 'Facility'
	WHEN 'SITE' THEN 'Site'
	WHEN 'BUILDING' THEN 'Building'
	WHEN 'FLOOR' THEN 'Floor'
	WHEN 'ROOM' THEN 'Room'
	WHEN 'LANDPARCEL' THEN 'Land Parcel'
	END AS "Level",
	
	CASE a.asset_type_code 
	WHEN 'ENVELOPE' THEN '0'
	WHEN 'FACILITY' THEN '1'
	WHEN 'SITE' THEN '2'
	WHEN 'BUILDING' THEN '2'
	WHEN 'FLOOR' THEN '3'
	WHEN 'ROOM' THEN '4'
	WHEN 'LANDPARCEL' THEN '-1'
	END AS "Level (info)",
	
	
    a.name AS "Code Name",
	
    CASE a.asset_type_code 
	WHEN 'ENVELOPE' THEN ltree2text(subltree(a.func_loc_path,0,1))
	WHEN 'FACILITY' THEN replace('F_' || ltree2text(subltree(a.func_loc_path,0,1)), '.', '-')
	WHEN 'SITE' THEN replace(ltree2text(subltree(a.func_loc_path,1,nlevel(a.func_loc_path))), '.', '-')
	WHEN 'BUILDING' THEN replace(ltree2text(subltree(a.func_loc_path,1,nlevel(a.func_loc_path))), '.', '-')
	WHEN 'FLOOR' THEN  replace(ltree2text(subltree(a.func_loc_path,1,nlevel(a.func_loc_path))), '.', '-')
	WHEN 'ROOM' THEN replace(ltree2text(subltree(a.func_loc_path,1,nlevel(a.func_loc_path))), '.', '-')
	WHEN 'LANDPARCEL' THEN replace(ltree2text(subltree(a.func_loc_path,1,nlevel(a.func_loc_path))), '.', '-')
	END AS "Code (UK)",
    
	location.longitude AS "Y",
	location.latitude AS "X",
	location.address AS "LocationAddress",
 	a_tp_e.suburb_code AS "Suburb_Id",
    	
	(SELECT v FROM asset.ref_district WHERE k = a_tp_e.district_code) AS "DISTRICT",
	(SELECT v FROM asset.ref_municipality WHERE k = a_tp_e.municipality_code) AS "LOCAL",
    (SELECT v FROM asset.ref_town WHERE k = a_tp_e.town_code) AS "TOWN",
 	(SELECT v FROM asset.ref_town WHERE k = a_tp_e.suburb_code) AS "SUBURB",
	
	a_tp_f.facility_type_code AS "AssetTypeID",
	(SELECT v FROM asset.ref_facility_type WHERE k = a_tp_f.facility_type_code ) AS "AssetTypeName",
		
	 lpi AS "MapFeatureID_UPDATED_BY_IMQS",
	 asset_classification.responsible_dept_code AS "Department",

	 geoms.geom AS "Geometry"
 
   FROM 
   		asset a
		LEFT JOIN location ON a.asset_id = location.asset_id
		LEFT JOIN asset_classification ON a.asset_id =  asset_classification.asset_id
		LEFT JOIN geoms ON a.asset_id = geoms.asset_id
		LEFT JOIN asset.a_tp_envelope a_tp_e ON a.asset_id = a_tp_e.asset_id
		LEFT JOIN asset.a_tp_facility a_tp_f ON a.asset_id = a_tp_f.asset_id
		
		LEFT JOIN asset.a_tp_landparcel a_tp_lp ON a.asset_id = a_tp_lp.asset_id

WHERE a.asset_type_code  <> 'LANDPARCEL'

ORDER BY "Level", func_loc_path ASC;
--ORDER BY func_loc_path ASC;