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




CREATE OR REPLACE FUNCTION public.fn_check_valid_func_loc_path(path ltree)
    RETURNS boolean
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    asset uuid;
BEGIN
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

ALTER TABLE public.asset ADD CONSTRAINT asset_check_path CHECK ((public.fn_check_valid_func_loc_path(func_loc_path) = true));

