CREATE OR REPLACE FUNCTION access_control.fn_get_system_user() RETURNS uuid AS $$
BEGIN
	RETURN (SELECT principal_id FROM access_control.principal WHERE name = 'System');
END; $$ LANGUAGE PLPGSQL
SECURITY DEFINER
;
//

COMMENT ON FUNCTION access_control.fn_get_system_user()
    IS 'The user named System is special as it is the only one created with the database. It is used to grant permissions';


CREATE OR REPLACE FUNCTION access_control.fn_get_audit_root_key() RETURNS uuid AS $$
BEGIN
  -- to keep someone from regenerating the entire audit trail from a known root we probably need a way to encrypt the first value
	RETURN uuid_generate_v4();
END; $$ LANGUAGE PLPGSQL
SECURITY DEFINER
;
//
COMMENT ON FUNCTION access_control.fn_get_audit_root_key()
    IS 'Not yet sure how we will generate this value or manage it. This function is a way to abstract this indecision from the audit log implementation';


CREATE OR REPLACE FUNCTION access_control.bit_or(integer, integer) RETURNS integer AS $$
BEGIN
	RETURN $1 | $2;
END; $$ LANGUAGE PLPGSQL;
//
COMMENT ON FUNCTION access_control.bit_or IS 'User defined aggregation to allow OR-ing access bits';
//

CREATE AGGREGATE b_or(int)
(
        INITCOND = 0,
        STYPE = int,
        SFUNC = access_control.bit_or
);
//


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
//
COMMENT ON FUNCTION access_control.fn_get_effective_access IS 'Given the principal `p` calculates the effective access (considering direct permissions and those via groups) to the  entity `e`';
//

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
//
COMMENT ON FUNCTION access_control.fn_get_effective_access IS 'Given the principal `p` calculates the effective access that my be granted to another (considering direct permissions and those via groups) to the  entity `e`';
//

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
		    INSERT INTO access_control.entity_access (entity_id, principal_id, access_types) VALUES (e, for_principal, access_mask) ON CONFLICT (entity_id, principal_id) DO NOTHING;
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
//

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
//
COMMENT ON FUNCTION access_control.sp_revoke_access IS 'Revoke the access of principal `for_principal` to all entities (e.g. asset_ids) in the list `to_entities uuid`';
//


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
//
COMMENT ON FUNCTION access_control.sp_add_group IS 'Add a new Group';
//

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
//
COMMENT ON FUNCTION access_control.sp_remove_group IS 'Remove a Group. Not allowed to do so if there are still users attached.';
//

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
//
COMMENT ON FUNCTION access_control.sp_add_user IS 'Add a new User. We must specify the uuid instead of the code as we need to use the indentity from Auth.';
//

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
//
COMMENT ON FUNCTION access_control.sp_remove_user IS 'Remove a User';
//

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
//

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
//