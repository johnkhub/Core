CREATE OR REPLACE VIEW public.asset_core_view AS
SELECT a.asset_id,
       a.asset_type_code,
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
//
COMMENT ON VIEW public.asset_core_view IS 'Inner join the basic core tables indicating what it is and where it is';
//