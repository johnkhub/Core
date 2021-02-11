--
-- This is a utility function that is not intended for production. It is intended to be useful not performant.
--
CREATE OR REPLACE FUNCTION public.fn_delete_asset(the_asset uuid) RETURNS void AS $$
DECLARE
    sub_classes  text[];
    link_tables text[];
    stmt text;
BEGIN
    DELETE FROM asset.asset_landparcel WHERE asset_id = the_asset;
    DELETE FROM asset.asset_landparcel WHERE landparcel_asset_id = the_asset;

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

-- We REDEFINE this function as we have a circular dependency.  fn_delete_asset needs to be able to log when it executes
-- but at the time the function is created the asset_schema may not yet exist
CREATE OR REPLACE FUNCTION public.log_audit(
    principal_id uuid,
    event_time timestamp without time zone,
    action text,
    status text,
    parameters jsonb,
    entity_id uuid
) RETURNS void AS $$
BEGIN
    PERFORM audit.log( principal_id,event_time,action,status,parameters,entity_id);
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;