--
-- This is a utility function that is not intended for production. It is intended to be useful not performant.
--
CREATE OR REPLACE FUNCTION public.fn_delete_asset(the_asset uuid) RETURNS void AS $$
DECLARE
    sub_classes  text[];
    link_tables text[];
    stmt text;
BEGIN
    sub_classes := 	ARRAY(SELECT code FROM public.assettype);
    FOR clss IN 1..array_upper(sub_classes,1)
        LOOP
            stmt := format('DELETE FROM asset.a_tp_%s WHERE asset_id=''%s''::UUID', sub_classes[clss], the_asset);
            EXECUTE stmt;
        END LOOP;

    DELETE FROM asset_link WHERE asset_id=the_asset;
    DELETE FROM asset_grouping WHERE asset_id=the_asset;
    DELETE FROM location WHERE asset_id=the_asset;
    DELETE FROM geoms WHERE asset_id=the_asset;
    DELETE FROM asset_identification WHERE asset_id=the_asset;
    DELETE FROM asset_classification WHERE asset_id=the_asset;
    DELETE FROM asset_tags WHERE asset_id = the_asset;

    DELETE FROM asset.asset_landparcel WHERE asset_id = the_asset;
    DELETE FROM asset.asset_landparcel WHERE landparcel_asset_id = the_asset;


    DELETE FROM access_control.entity_access WHERE entity_id = the_asset;

    link_tables := ARRAY (SELECT table_name::text FROM information_schema.tables WHERE table_schema = 'dtpw' AND table_name like '%_link');
    FOR lnk IN 1..array_upper(link_tables,1)
        LOOP
            stmt := format('DELETE FROM dtpw.%s WHERE asset_id=''%s''::UUID', link_tables[lnk], the_asset);
            EXECUTE stmt;
        END LOOP;

    DELETE FROM public.quantity WHERE asset_id = the_asset;

    DELETE FROM asset WHERE asset_id=the_asset;

    -- update audit trail
    PERFORM log_audit(
        (SELECT access_control.fn_get_system_user()),
        NOW()::timestamp without time zone,
        'DELETE_ASSET'::text,
        'HARD_DELETE'::text,
        format('{"asset": "''%s''"}', the_asset)::jsonb,
        the_asset
    );
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

CREATE OR REPLACE FUNCTION public.fn_is_valid_func_loc_path(path ltree) RETURNS boolean AS $$
DECLARE
    asset uuid;
    --msg text;
BEGIN
    FOR i IN 1..public.nlevel(path)
        LOOP
            asset := (SELECT asset_id from public.asset WHERE code = REPLACE(public.subpath(path,0,i)::text, '.', '-'));
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

COMMENT ON FUNCTION public.fn_is_valid_func_loc_path IS 'Identifies if a segment of a path does not have a corresponding asset associated with it. E.g. select * from h(''11567.11567.BB.L1.09''::ltree) or select * into broken_paths from asset where fn_is_valid_func_loc_path(func_loc_path) = false';

CREATE OR REPLACE FUNCTION public.fn_identify_no_subclasses(the_asset uuid) RETURNS boolean AS $$
DECLARE
    sub_classes  text[];
    stmt text;
    num int;
    total int;
BEGIN
    num := 0;
    total := 0;
    sub_classes := 	ARRAY(SELECT code FROM public.assettype);
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

CREATE OR REPLACE FUNCTION log_audit(
    principal_id uuid,
    event_time timestamp without time zone,
    action text,
    status text,
    parameters jsonb,
    entity_id uuid
) RETURNS void AS $$
BEGIN
    RAISE EXCEPTION 'Function public.log_audit should have been redefined upon loading of the audit schema!';
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;