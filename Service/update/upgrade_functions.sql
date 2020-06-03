DROP FUNCTION IF EXISTS access_control.fn_get_system_user;
DROP FUNCTION IF EXISTS access_control.fn_get_audit_root_key;
DROP FUNCTION IF EXISTS access_control.fn_get_effective_access;
DROP FUNCTION IF EXISTS access_control.fn_get_effective_grant;
DROP FUNCTION IF EXISTS access_control.sp_grant_access;
DROP FUNCTION IF EXISTS access_control.sp_revoke_access;
DROP FUNCTION IF EXISTS access_control.sp_add_group;
DROP FUNCTION IF EXISTS access_control.sp_remove_group;
DROP FUNCTION IF EXISTS access_control.sp_add_user;
DROP FUNCTION IF EXISTS access_control.sp_remove_user;
DROP FUNCTION IF EXISTS access_control.sp_add_user_to_group;
DROP FUNCTION IF EXISTS access_control.sp_remove_user_from_group;
DROP FUNCTION IF EXISTS access_control.sp_add_remove_user_from_group;
DROP AGGREGATE b_or(int);
DROP FUNCTION access_control.bit_or;

CREATE OR REPLACE FUNCTION access_control.fn_get_system_user() RETURNS uuid AS $$
BEGIN
    RETURN (SELECT principal_id FROM access_control.principal WHERE name = 'System');
END; $$ LANGUAGE PLPGSQL
    SECURITY DEFINER
;


COMMENT ON FUNCTION access_control.fn_get_system_user()
    IS 'The user named System is special as it is the only one created with the database. It is used to grant permissions';


CREATE OR REPLACE FUNCTION access_control.fn_get_audit_root_key() RETURNS uuid AS $$
BEGIN
    -- to keep someone from regenerating the entire audit trail from a known root we probably need a way to encrypt the first value
    RETURN uuid_generate_v4();
END; $$ LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.fn_get_audit_root_key()
    IS 'Not yet sure how we will generate this value or manage it. This function is a way to abstract this indecision from the audit log implementation';


CREATE OR REPLACE FUNCTION access_control.bit_or(integer, integer) RETURNS integer AS $$
BEGIN
    RETURN $1 | $2;
END; $$ LANGUAGE PLPGSQL;

COMMENT ON FUNCTION access_control.bit_or IS 'User defined aggregation to allow OR-ing access bits';


CREATE AGGREGATE b_or(int)
    (
    INITCOND = 0,
    STYPE = int,
    SFUNC = access_control.bit_or
    );



CREATE OR REPLACE FUNCTION access_control.fn_get_effective_access(p uuid, e uuid) RETURNS integer AS $$
BEGIN
    RETURN (
        SELECT
            b_or(access_types)
        FROM access_control.entity_access
        WHERE entity_id = e AND	principal_id IN
                                   (
                                       SELECT g.principal_id FROM
                                           access_control.principal u LEFT JOIN access_control.principal g ON u.group_id = g.id
                                       WHERE u.principal_id = p
                                       UNION SELECT principal_id FROM access_control.principal WHERE principal_id = p
                                   )
    );
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.fn_get_effective_access IS 'Given the principal `p` calculates the effective access (considering direct permissions and those via groups) to the  entity `e`';


CREATE OR REPLACE FUNCTION access_control.fn_get_effective_grant(p uuid, e uuid) RETURNS integer AS $$
BEGIN
    RETURN (
        SELECT
            b_or(grant_types)
        FROM access_control.entity_access
        WHERE entity_id = e AND	principal_id IN
                                   (
                                       SELECT g.principal_id FROM
                                           access_control.principal u LEFT JOIN access_control.principal g ON u.group_id = g.id
                                       WHERE u.principal_id = p
                                       UNION SELECT principal_id FROM access_control.principal WHERE principal_id = p
                                   )
    );
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.fn_get_effective_access IS 'Given the principal `p` calculates the effective access that my be granted to another (considering direct permissions and those via groups) to the  entity `e`';


CREATE OR REPLACE FUNCTION access_control.sp_grant_access(grantor uuid, access_mask integer, to_entities uuid[], for_principal uuid) RETURNS void AS $$
DECLARE
    g int;
    e uuid;
    msg text;
    a_id uuid;
BEGIN
    -- Note the special use of the system user: it would be crazy to have to assign permission to all entities in the system to this user
    FOREACH e IN ARRAY to_entities LOOP
            IF (grantor = access_control.fn_get_system_user()) OR (access_mask & access_control.fn_get_effective_grant(grantor,e) = access_mask) THEN
                INSERT INTO access_control.entity_access (entity_id, principal_id, access_types) VALUES (e, for_principal, access_mask);
                msg := grantor::text ||' granted ' || access_mask || ' access of ' || e || ' to ' || for_principal;
                --INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Success', CURRENT_TIMESTAMP) RETURNING audit_id INTO a_id;
                --INSERT INTO audit.auditlink (asset_id, audit_id) VALUES (e, a_id); Audit kink must not have FK to asset - it must also use the entity abstraction asset types are not assets but they are entities
            ELSE
                msg := grantor::text ||' granted ' || access_mask || ' access of ' || e || ' to ' || for_principal;
                --INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Failed', CURRENT_TIMESTAMP) RETURNING audit_id INTO a_id;
                --INSERT INTO audit.auditlink (asset_id, audit_id) VALUES (e, a_id);
                RAISE EXCEPTION 'Principal % does not poses grant permissions % to entity %', grantor, access_mask, e;
            END IF;
        END loop;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;


CREATE OR REPLACE FUNCTION access_control.sp_revoke_access(revoker uuid, to_entities uuid[], for_principal uuid) RETURNS void AS $$
DECLARE
    g int;
    e uuid;
    msg text;
    a_id uuid;
BEGIN
    FOREACH e IN ARRAY to_entities LOOP
            IF (revoker = access_control.fn_get_system_user()) OR (access_mask & access_control.fn_get_effective_grant(revoker,e) = access_mask) THEN
                DELETE FROM access_control.entity_access WHERE entity_id = e AND principal_id = for_principal;
                msg := revoker::text ||' granted ' || access_mask || ' access of ' || e || ' to ' || for_principalpal_id;
                --INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Success', CURRENT_TIMESTAMP) RETURNING audit_id INTO a_id;
                --INSERT INTO audit.auditlink (asset_id, audit_id) VALUES (e, a_id);
            ELSE
                msg := revoker::text ||' revoked ' || access_mask || ' access of ' || e || ' to ' || for_principalpal_id;
                --INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Failed', CURRENT_TIMESTAMP);
                --INSERT INTO audit.auditlink (asset_id, audit_id) VALUES (e, a_id);
                RAISE EXCEPTION 'Principal % does not poses grant permissions % to entity %', revoker, access_mask, e;
            END IF;
        END loop;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.sp_revoke_access IS 'Revoke the access of principal `for_principal` to all entities (e.g. asset_ids) in the list `to_entities uuid`';



CREATE OR REPLACE FUNCTION access_control.sp_add_group(code varchar(10), description text DEFAULT NULL)  RETURNS uuid AS $$
DECLARE
    group_id uuid;
BEGIN
    group_id := uuid_generate_v4();
    INSERT INTO access_control.principal(principal_id, name, description, is_group) VALUES (group_id, code, description, true);

    RETURN group_id;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.sp_add_group IS 'Add a new Group';


CREATE OR REPLACE FUNCTION access_control.sp_remove_group(code varchar(10))  RETURNS void AS $$
DECLARE
    g_id uuid;
    user_count int;
BEGIN
    -- Must add a way to move users to another group so we can delete this group
    g_id := (SELECT principal_id FROM access_control.principal WHERE name = code);
    user_count := (SELECT count(*) FROM access_control.principal WHERE group_id = (SELECT group_id FROM access_control.principal WHERE principal_id = g_id));
    IF EXISTS(SELECT reserved FROM access_control.principal WHERE principal_id = g_id AND reserved = true) THEN
        RAISE EXCEPTION 'Cannot delete reserved group %. ', code;
    END IF;

    IF (user_count > 0) THEN
        RAISE EXCEPTION 'Cannot delete group %. It has % users attached.', code, user_count;
    END IF;

    DELETE FROM access_control.entity_access WHERE principal_id = g_id;
    DELETE FROM access_control.principal WHERE principal_id = g_id;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.sp_remove_group IS 'Remove a Group. Not allowed to do so if there are still users attached.';


CREATE OR REPLACE FUNCTION access_control.sp_add_user(user_id uuid, code varchar(10), description text DEFAULT NULL)  RETURNS void AS $$
BEGIN
    IF UPPER(code) = 'SYSTEM' THEN
        RAISE EXCEPTION 'Cannot add user. % is a reserved name. %', code, e;
    END IF;
    INSERT INTO access_control.principal(principal_id, name, description, is_group) VALUES (user_id, code, description, false);
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.sp_add_user IS 'Add a new User. We must specify the uuid instead of the code as we need to use the indentity from Auth.';


CREATE OR REPLACE FUNCTION access_control.sp_remove_user(code varchar(10))  RETURNS void AS $$
DECLARE
    user_id uuid;
BEGIN
    IF UPPER(code) = 'SYSTEM' THEN
        RAISE EXCEPTION 'Cannot remove user. % is a reserved name. %', code, e;
    END IF;
    user_id := (SELECT user_id FROM access_control.principal WHERE name = code);
    DELETE FROM access_control.entity_access WHERE principal_id = user_id;
    DELETE FROM access_control.principal WHERE principal_id = user_id;

END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION access_control.sp_remove_user IS 'Remove a User';


CREATE OR REPLACE FUNCTION access_control.sp_add_user_to_group(user_id uuid, group_name varchar(10))  RETURNS void AS $$
DECLARE
    g_id int;
BEGIN
    IF EXISTS(SELECT id FROM access_control.principal WHERE principal_id = user_id) THEN
        g_id := (SELECT id FROM access_control.principal WHERE name = group_name);
        IF g_id IS NOT NULL THEN
            UPDATE access_control.principal SET group_id = g_id WHERE principal_id = user_id;
        ELSE
            RAISE EXCEPTION 'Group % does not exist as a principal.', group_name;
        END IF;
    ELSE
        RAISE EXCEPTION 'User % does not exist as a principal.', user_id;
    END IF;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;


CREATE OR REPLACE FUNCTION access_control.sp_remove_user_from_group(user_id uuid, group_name varchar(10))  RETURNS void AS $$
DECLARE
    g_id int;
BEGIN
    IF EXISTS(SELECT id FROM access_control.principal WHERE principal_id = user_id) THEN
        g_id := (SELECT id FROM access_control.principal WHERE name = group_name);
        IF g_id IS NOT NULL THEN
            UPDATE access_control.principal SET group_id = null WHERE principal_id = user_id AND  group_id = g_id;
        ELSE
            RAISE EXCEPTION '% does not exist as a principal. %', group_name, e;
        END IF;
    ELSE
        RAISE EXCEPTION '% does not exist as a principal %', user_id, e;
    END IF;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;



DROP FUNCTION audit.fn_add_audit_row CASCADE;

CREATE OR REPLACE FUNCTION audit.fn_add_audit_row()
    RETURNS trigger AS
$$
DECLARE
    previous text;
BEGIN
    -- Get has of previous row, calculate hash on new row (including previous row hash)
    previous := (SELECT tamper_check FROM audit.audit ORDER BY insert_time DESC LIMIT 1);
    IF previous IS NULL THEN
        previous := access_control.fn_get_audit_root_key();
    END IF;

    NEW.tamper_check = md5(CAST((NEW.*)AS text) || previous);
    RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

COMMENT ON FUNCTION "audit".fn_add_audit_row IS 'Trigger function that calculates the hash of the audit row (including that of previous row)';


CREATE TRIGGER on_audit_insert
    BEFORE insert
    ON audit.audit
    FOR EACH ROW EXECUTE PROCEDURE audit.fn_add_audit_row();



CREATE OR REPLACE FUNCTION public.fn_add_tags(a uuid, t text[]) RETURNS void AS $$
DECLARE
    invalid text[];
BEGIN
    -- identify the values in t that are not in the table of defined tags
    invalid := 	ARRAY(SELECT u FROM unnest(ARRAY[t]) u LEFT JOIN tags ON u = tags.k WHERE tags.k IS NULL);
    IF (array_length(invalid,1) > 0) THEN
        RAISE EXCEPTION 'Tag(s) % are not defined', invalid;
    END IF;

    INSERT INTO asset_tags (asset_id,tags) VALUES (a,t)
    ON CONFLICT (asset_id)
        DO
            UPDATE SET tags = EXCLUDED.tags ||   -- append the values not already present
                              ARRAY(
                                  -- This expands the arrays into rows and joins them to determine which ones are new
                                      SELECT to_add FROM
                                          unnest(t) to_add
                                              LEFT JOIN
                                          unnest(asset_tags.tags) existing
                                          ON existing = to_add WHERE existing IS NULL
                                  ),
                       asset_id = EXCLUDED.asset_id;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

COMMENT ON FUNCTION public.fn_add_tags IS 'Adds the specified tags to the asset with the specified UUID';

CREATE OR REPLACE FUNCTION public.fn_has_tags(a uuid, t text[]) RETURNS boolean AS $$
BEGIN
    RETURN coalesce((SELECT tags FROM asset_tags WHERE asset_id = a) @> t, false); --@> means contains
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

CREATE OR REPLACE FUNCTION public.fn_remove_tags(a uuid, t text[]) RETURNS void AS $$
DECLARE
    invalid text[];
BEGIN
    -- identify the values in t that are not in the table of defined tags
    invalid := 	ARRAY(SELECT u FROM unnest(ARRAY[t]) u LEFT JOIN tags ON u = tags.k WHERE tags.k IS NULL);
    IF (array_length(invalid,1) > 0) THEN
        RAISE EXCEPTION 'Tag(s) % are not defined', invalid;
    END IF;

    FOR i IN 1..array_upper(t,1)
        LOOP
            UPDATE asset_tags SET tags = array_remove(tags, t[i]) WHERE asset_id = a;
        END LOOP;

    -- there is a fk constraint between asset and asset_tag, so we need to remove the dead entry to be able
    -- to remove the asset
    DELETE FROM asset_tags WHERE asset_id = a AND cardinality(tags) = 0; -- "array_length(tags,1) = 0;" doe snot work!
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;

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


--select * from fn_is_valid_func_loc_path('11567.11567.BB.L1.09'::ltree)

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


CREATE OR REPLACE FUNCTION public.f_unaccent(text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE PARALLEL SAFE STRICT
AS $function$
SELECT public.unaccent('public.unaccent', $1)  -- schema-qualify function and dictionary
$function$
;






