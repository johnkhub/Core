CREATE OR REPLACE VIEW asset.asset_core_view_internal AS
SELECT
    public.asset.asset_id,
    public.asset.asset_type_code,
    public.asset.code,
    public.asset.name,
    public.asset.description,
    public.asset.adm_path,
    public.asset.grap_path,
    public.asset.func_loc_path,
    public.asset.creation_date,
    public.asset.deactivated_at,

    public.location.latitude,
    public.location.longitude,
    public.location.address,
    public.location.region_code,
    public.location.district_code,
    public.location.town_code,
    public.location.suburb_code,
    public.location.municipality_code,
    public.location.ward_code,

    public.geoms.geom,

    public.asset_identification.barcode,
    public.asset_identification.serial_number,

    asset.a_tp_facility.facility_type_code,
    asset.a_tp_landparcel.lpi

FROM
    public.asset
        LEFT JOIN public.location ON asset.asset_id = location.asset_id
        LEFT JOIN public.asset_identification ON asset_identification.asset_id = asset.asset_id
        LEFT JOIN public.geoms ON geoms.asset_id = asset.asset_id
        --LEFT JOIN asset.a_tp_building ON asset.asset_id = a_tp_building.asset_id
--LEFT JOIN asset.a_tp_component ON asset.asset_id = a_tp_component.asset_id
--LEFT JOIN asset.a_tp_envelope ON asset.asset_id = a_tp_envelope.asset_id
        LEFT JOIN asset.a_tp_facility ON asset.asset_id = a_tp_facility.asset_id
--LEFT JOIN asset.a_tp_floor ON a.asset_id = a_tp_floor.asset_id
        LEFT JOIN asset.a_tp_landparcel ON asset.asset_id = a_tp_landparcel.asset_id
--LEFT JOIN asset.a_tp_room ON asset.asset_id = a_tp_room.asset_id
--LEFT JOIN asset.a_tp_site ON asset.asset_id = a_tp_site.asset_id
ORDER BY func_loc_path ASC;
//
CREATE OR REPLACE VIEW asset.landparcel_view AS
SELECT
    a.*, p.lpi
FROM asset a JOIN asset.a_tp_landparcel p ON a.asset_id = p.asset_id;
//