--  THESE NEED TO GO INTO THE changelog FILES AS WELL
CREATE OR REPLACE FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) RETURNS boolean AS $$
DECLARE
    sub_classes  text[];
    stmt text;
    num int;
    total int;
BEGIN
    num := 0;
    total := 0;
    sub_classes := 	ARRAY(SELECT code FROM assettype);
    FOR clss IN 1..array_upper(sub_classes,1)
        LOOP
            stmt := format('SELECT count(asset_id) FROM asset.a_tp_%s WHERE asset_id=''%s''::UUID', sub_classes[clss], the_asset);
            EXECUTE stmt into num;
            total := total + num;
        END LOOP;

    --raise notice 'Number %', total;
    RETURN total > 1;
END ; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION public.fn_identify_multiple_subclasses IS 'Identifies if the same asset has multiple subclasses e.g. same uuid in say envelope and facility. This is an invalid state but at the moment there is nothing in the database constraints that stops you from doing this.';



ALTER TABLE asset.ref_ward ALTER COLUMN local_municipality_k SET NOT NULL;
ALTER TABLE asset.ref_suburb ALTER COLUMN town_k SET NOT NULL;
ALTER TABLE asset.ref_suburb ALTER COLUMN ward_k SET NOT NULL;
ALTER TABLE asset.ref_town ALTER COLUMN local_municipality_k SET NOT NULL;
ALTER TABLE asset.ref_municipality ALTER COLUMN district_k SET NOT NULL;

ALTER TABLE public.asset ADD CONSTRAINT asset_check_path CHECK ((public.fn_check_valid_func_loc_path(func_loc_path) = true));

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

-- Need to revisit it as we still can't import it directly like this
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
   ST_asText(asset_core_dtpw_view.geom) AS geom
FROM dtpw.asset_core_dtpw_view;

COMMENT ON VIEW dtpw.dtpw_export_view IS 'This is an exact copy of the select in dtpw.dtpw_core_report_view but it converts the geometry to well-known text.';



CREATE OR REPLACE FUNCTION public.fn_identify_no_subclasses(the_asset uuid) RETURNS boolean AS $$
DECLARE
    sub_classes  text[];
    stmt text;
    num int;
    total int;
BEGIN
    num := 0;
    total := 0;
    sub_classes := 	ARRAY(SELECT code FROM assettype);
    FOR clss IN 1..array_upper(sub_classes,1)
        LOOP
            stmt := format('SELECT count(asset.asset_id) FROM asset join asset.a_tp_%s s ON asset.asset_id = s.asset_id AND asset_type_code = ''%s'' WHERE asset.asset_id=''%s''::UUID', sub_classes[clss], sub_classes[clss], the_asset);
            EXECUTE stmt into num;
            total := total + num;
        END LOOP;

    --raise notice 'Number %', total;
    RETURN total = 0;
END ; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;