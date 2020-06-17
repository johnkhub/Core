DELETE FROM databasechangelog;
DELETE FROM databasechangeloglock;

DROP TABLE "ORMJS_CACHE_LOCK_V1";
DROP TABLE "ORMJS_CACHE_INDEX_V1";
DROP TABLE asset_import;

DROP TABLE far.financials;
DROP TABLE far.lifecycle;
DROP SCHEMA far;

DROP TABLE transactions.transaction_batch CASCADE;
DROP TABLE transactions.transaction;
DROP TABLE transactions.field;
DROP TABLE transactions.transaction_type;
DROP SCHEMA transactions CASCADE;

DROP TABLE access_control.user;
DROP TABLE access_control.group;

COMMENT ON TABLE access_control.access_type IS 'Master data list of access types';
COMMENT ON COLUMN access_control.access_type.mask IS 'NONE=0  CREATE=1 READ=2,  UPDATE=4,  DELETE=8, ...';
COMMENT ON TABLE access_control.entity_access IS 'Defines what access specific principals have to specific entities';
COMMENT ON COLUMN access_control.entity_access.access_types IS 'Principal has these';
COMMENT ON COLUMN access_control.entity_access.grant_types IS 'Principal may grant these to others';
COMMENT ON TABLE access_control.principal IS 'Self referential table of principals. Groups are also principals. The table links users to groups via this self-reference';

ALTER TABLE public.asset DROP CONSTRAINT check_paths;


COMMENT ON FUNCTION public.fn_is_valid_func_loc_path(path public.ltree) IS 'Identifies if a segment of a path does not have a corresponding asset associated with it. E.g. select * from h(''11567.11567.BB.L1.09''::ltree) or select * into broken_paths from asset where fn_is_valid_func_loc_path(func_loc_path) = false';

CREATE INDEX asset_name_idx ON public.asset USING btree (name);

CREATE OR REPLACE FUNCTION public.fn_check_valid_func_loc_path(path ltree)
    RETURNS boolean
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    asset uuid;
BEGIN
    -- If n=1 this is the root of the path and obviously won't exist
    IF nlevel(path) = 1 THEN
        RETURN true;
    END IF;

    -- Only check up to n-1 as n is the node we are trying to insert
    FOR i IN 1..nlevel(path)-1
        LOOP
            asset := (SELECT asset_id from asset WHERE code = REPLACE(subpath(path,0,i)::text, '.', '-'));
            IF (asset IS null) THEN RETURN false;
            END IF;
        END LOOP;
    RETURN true;
END; $function$
;

COMMENT ON FUNCTION public.fn_check_valid_func_loc_path IS 'Variation of fn_is_valid_func_loc_path, that is suitable fro CHECK constraint. It will only check if the path > 1 segment and only up to n-1 segments.';

CREATE FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
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

    raise notice 'Number %', total;

END ; $$;

COMMENT ON FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) IS 'Identifies if the same asset has multiple subclasses e.g. same uuid in say envelope and facility. This is an invalid state but at the moment there is nothing in the database constraints that stops you from doing this.';



DROP INDEX asset_unaccent_name_idx;
DROP INDEX location_lower_idx;

DROP INDEX asset_asset_id_idx;


DROP MATERIALIZED VIEW dtpw.dtpw_core_report_view;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view_with_lpi;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view;
DROP VIEW IF EXISTS public.asset_core_view;
DROP VIEW IF EXISTS public.import_report_view;

alter table public.location alter column latitude type numeric(11,8);
alter table public.location alter column longitude type numeric(11,8);