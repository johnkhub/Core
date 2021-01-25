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

DROP TRIGGER IF EXISTS on_asset_update ON public.asset;
CREATE TRIGGER on_asset_update
    BEFORE update ON public.asset
    FOR EACH ROW
EXECUTE PROCEDURE fn_asset_update();


