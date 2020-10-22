CREATE OR REPLACE VIEW public.asset_core_view AS
SELECT a.asset_id,
       a.asset_type_code,
       a.name,
       a.func_loc_path,
       a.deactivated_at IS NULL AS active,
       a.description,

       location.latitude,
       location.longitude,
       location.address,
       location.region_code,
       location.district_code,
       location.town_code,
       location.suburb_code,
       location.municipality_code,

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


CREATE OR REPLACE VIEW public.quantities_view AS
SELECT
    q.name as name, q.num_units as num_units,
    u.code as unit_code, u.name as unit_name, u.is_si as is_si, u.symbol as symbol, u.type as unit_type
FROM
    public.quantity q
        JOIN unit u ON q.unit_code = u.code
;
//
