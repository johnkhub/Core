-- TODO The community edition of liquibase does not support adding CHECK constraints, this is why I am adding them here
ALTER TABLE public.asset_identification ADD CONSTRAINT asset_identification_barcode_check CHECK (barcode::text <> ''::text);
ALTER TABLE public.asset_identification ADD CONSTRAINT asset_identification_serial_number_check CHECK (serial_number::text <> ''::text);
ALTER TABLE public.kv_base ADD CONSTRAINT kv_base_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.unit ADD CONSTRAINT unit_code_check CHECK (code::text ~ '^[\w]*$'::text);
ALTER TABLE public.unit ADD CONSTRAINT unit_name_check CHECK (name::text <> ''::text);
ALTER TABLE public.unit  ADD CONSTRAINT unit_symbol_check CHECK (symbol::text <> ''::text);
ALTER TABLE public.tags ADD CONSTRAINT tags_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);

ALTER TABLE kv_type ADD CONSTRAINT kv_type_code_check CHECK ((((code)::text <> ''::text) AND ((code)::text ~ '^[\w]*$'::text)));
ALTER TABLE kv_type ADD CONSTRAINT kv_type_name_check CHECK (((name)::text <> ''::text));
ALTER TABLE kv_type ADD CONSTRAINT kv_type_table_check CHECK (table_exists(("table")::text));

ALTER TABLE public.ref_district ADD CONSTRAINT ref_district_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_municipality ADD CONSTRAINT ref_municipality_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_region ADD CONSTRAINT ref_region_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_suburb ADD CONSTRAINT ref_suburb_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_town ADD CONSTRAINT ref_town_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_ward ADD CONSTRAINT ref_ward_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);



ALTER TABLE public.ref_accessibility_rating ADD CONSTRAINT ref_accessibility_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_asset_class ADD CONSTRAINT ref_asset_class_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_asset_nature ADD CONSTRAINT ref_asset_nature_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_condition_rating ADD CONSTRAINT ref_condition_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_confidence_rating ADD CONSTRAINT ref_confidence_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_criticality_rating ADD CONSTRAINT ref_criticality_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.ref_utilisation_rating ADD CONSTRAINT ref_utilisation_rating_k_check  CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);




//
CREATE OR REPLACE FUNCTION public.fn_check_valid_func_loc_path(path ltree)
    RETURNS boolean
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    asset uuid;
    idx int;
BEGIN
    -- During a restore, data is inserted before indexes are created. Since the constraint checks against
    -- data in the system checking this constraint is catastrophically slow and ultimately useless as it
    -- as we are restoring a database that is already valid.
    idx := (
        SELECT
            count(t.relname) as table_name
        FROM
            pg_class t,  pg_class i,  pg_index ix,  pg_attribute a
        WHERE
          t.oid = ix.indrelid
          AND i.oid = ix.indexrelid
          AND a.attrelid = t.oid
          AND a.attnum = ANY(ix.indkey)
          AND t.relkind = 'r'
          AND t.relname = 'asset'
    );
    IF idx = 0 THEN
        RETURN true;
    END IF;

    -- If n=1 this is the root of the path and obviously won't exist
    IF public.nlevel(path) = 1 THEN
        RETURN true;
    END IF;

    -- Only check up to n-1 as n is the node we are trying to insert
    FOR i IN 1..public.nlevel(path)-1
        LOOP
            asset := (SELECT asset_id from public.asset WHERE code = REPLACE(public.subpath(path,0,i)::text, '.', '-'));
            IF (asset IS null) THEN RETURN false;
            END IF;
        END LOOP;
    RETURN true;
END; $function$
;

COMMENT ON FUNCTION public.fn_check_valid_func_loc_path IS 'Variation of fn_is_valid_func_loc_path, that is suitable fro CHECK constraint. It will only check if the path > 1 segment and only up to n-1 segments.';

ALTER TABLE public.asset ADD CONSTRAINT asset_check_func_loc_path CHECK ((public.fn_check_valid_func_loc_path(func_loc_path) = true));


CREATE OR REPLACE FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) RETURNS boolean AS $$
DECLARE
    sub_classes  text[];
    stmt text;
    num int;
    total int;
    idx int;
BEGIN
    -- During a restore, data is inserted before indexes are created. Since the constraint checks against
    -- data in the system checking this constraint is catastrophically slow and ultimately useless as it
    -- as we are restoring a database that is already valid.
    idx := (
        SELECT
            count(t.relname) as table_name
        FROM
            pg_class t,  pg_class i,  pg_index ix,  pg_attribute a
        WHERE
                t.oid = ix.indrelid
          AND i.oid = ix.indexrelid
          AND a.attrelid = t.oid
          AND a.attnum = ANY(ix.indkey)
          AND t.relkind = 'r'
          AND t.relname = 'asset'
    );

    IF idx = 0 THEN
        RETURN false;
    END IF;

    num := 0;
    total := 0;
    sub_classes := 	ARRAY(SELECT code FROM public.assettype);
    FOR clss IN 1..coalesce(array_upper(sub_classes,1),0)
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
//
COMMENT ON FUNCTION public.fn_identify_multiple_subclasses IS 'Identifies if the same asset has multiple subclasses e.g. same uuid in say envelope and facility. This is an invalid state but at the moment there is nothing in the database constraints that stops you from doing this.';

ALTER TABLE public.asset ADD CONSTRAINT asset_subclass CHECK (public.fn_identify_multiple_subclasses(asset_id) = false);


CREATE OR REPLACE FUNCTION fn_asset_update() RETURNS trigger AS $$
BEGIN
    IF OLD.asset_type_code <> NEW.asset_type_code THEN
        RAISE EXCEPTION 'Changing asset_type_code is not allowed. Tried to change % to % to for entity %', OLD.asset_type_code, NEW.asset_type_code, OLD.asset_id;
    END IF;
    RETURN NEW;
END ; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;
//



CREATE TRIGGER on_asset_update
    BEFORE update ON public.asset
    FOR EACH ROW
    EXECUTE PROCEDURE fn_asset_update();
//

