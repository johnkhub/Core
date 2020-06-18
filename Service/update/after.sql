--  THESE NEED TO GO INTO THE changelog FILES AS WELL

ALTER TABLE asset.ref_ward ALTER COLUMN local_municipality_k SET NOT NULL;
ALTER TABLE asset.ref_suburb ALTER COLUMN town_k SET NOT NULL;
ALTER TABLE asset.ref_suburb ALTER COLUMN ward_k SET NOT NULL;
ALTER TABLE asset.ref_town ALTER COLUMN local_municipality_k SET NOT NULL;
ALTER TABLE asset.ref_municipality ALTER COLUMN district_k SET NOT NULL;

ALTER TABLE public.asset ADD CONSTRAINT asset_check CHECK ((public.fn_check_valid_func_loc_path(func_loc_path) = true));

ALTER TABLE asset.a_tp_building ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_component ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_envelope ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_facility ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_floor ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_landparcel ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_room ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);
ALTER TABLE asset.a_tp_site ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);

ALTER TABLE public.asset_classification
    ADD CONSTRAINT fk_asset FOREIGN KEY (asset_id)
        REFERENCES public.asset (asset_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;


CREATE OR REPLACE VIEW dtpw.dtpw_export_view AS
SELECT
   asset_core_dtpw_view.asset_id,
   asset_core_dtpw_view.asset_type_code,
   asset_core_dtpw_view.name,
   asset_core_dtpw_view.func_loc_path,
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
   ST_AsEWKT(asset_core_dtpw_view.geom) AS geom
FROM dtpw.asset_core_dtpw_view;

COMMENT ON VIEW dtpw.dtpw_export_view IS 'This is an exact copy of the select in dtpw.dtpw_core_report_view but it converts the geometry to well-known text.';

DELETE FROM asset_classification WHERE asset_id IN
(
   SELECT c.asset_id FROM asset_classification c LEFT JOIN asset a ON c.asset_id = a.asset_id WHERE a.asset_id IS NULL
);