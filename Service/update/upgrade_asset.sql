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

ALTER TABLE asset.ref_district ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_facility_type ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_municipality ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_region ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_suburb ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_town ALTER COLUMN activated_at SET DEFAULT now();
ALTER TABLE asset.ref_ward ALTER COLUMN activated_at SET DEFAULT now();

ALTER TABLE asset.ref_district ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_facility_type ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_municipality ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_region ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_suburb ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_town ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE asset.ref_ward ALTER COLUMN creation_date SET DEFAULT now();

ALTER TABLE asset.a_tp_landparcel DROP CONSTRAINT a_tp_landparcel_lpi_key;
CREATE UNIQUE INDEX lpi_idx ON asset.a_tp_landparcel USING btree (lpi);