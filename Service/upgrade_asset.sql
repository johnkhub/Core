-- asset.landparcel_view source

CREATE OR REPLACE VIEW asset.landparcel_view
AS SELECT a.asset_id,
    a.asset_type_code,
    a.code,
    a.name,
    a.adm_path,
    a.func_loc_path,
    a.grap_path,
    a.creation_date,
    a.deactivated_at,
    a.reference_count,
    p.lpi
   FROM asset a
     JOIN asset.a_tp_landparcel p ON a.asset_id = p.asset_id;

ALTER TABLE asset.ref_district ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_facility_type ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_municipality ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_region ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_suburb ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_town ADD activated_at timestamp NULL DEFAULT now();
ALTER TABLE asset.ref_ward ADD activated_at timestamp NULL DEFAULT now();