--
-- This is a utility function that is not intended for production. It is intended to be useful not performant.
--
CREATE OR REPLACE FUNCTION public.fn_delete_asset(the_asset uuid) RETURNS void AS $$
DECLARE
    sub_classes  text[];
    stmt text;
BEGIN
    sub_classes := 	ARRAY(SELECT code FROM assettype);
    FOR clss IN 1..array_upper(sub_classes,1)
        LOOP
            stmt := format('DELETE FROM asset.a_tp_%s WHERE asset_id=''%s''::UUID', sub_classes[clss], the_asset);
            EXECUTE stmt;
        END LOOP;

    DELETE FROM asset_link WHERE asset_id=the_asset;
    DELETE FROM location WHERE asset_id=the_asset;
    DELETE FROM geoms WHERE asset_id=the_asset;
    DELETE FROM asset_identification WHERE asset_id=the_asset;
    DELETE FROM asset_classification WHERE asset_id=the_asset;
    DELETE FROM asset WHERE asset_id=the_asset;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;


select * from fn_is_valid_func_loc_path('11567.11567.BB.L1.09'::ltree)

CREATE OR REPLACE FUNCTION public.fn_is_valid_func_loc_path(path ltree) RETURNS boolean AS $$
DECLARE
    asset uuid;
    msg text;
BEGIN
    FOR i IN 1..nlevel(path)
        LOOP
            asset := (SELECT asset_id from asset WHERE code = REPLACE(subpath(path,0,i)::text, '.', '-'));
            --select replace(subpath(path,0,i)::text, '.','-') into msg;
            --raise notice 'hallo %', msg;

            IF (asset IS null) THEN RETURN false;
            END IF;
        END LOOP;
    RETURN true;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;


select *
into broken_paths
from asset
where fn_is_valid_func_loc_path(func_loc_path) = false


