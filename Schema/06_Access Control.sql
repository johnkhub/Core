CREATE SCHEMA "access_control";

CREATE TABLE "access_control"."access_type" (
  "name" varchar(10) PRIMARY KEY,
  "mask" integer NOT NULL
);

CREATE TABLE "access_control"."entity_access" (
  "entity_id" uuid,
  "principal_id" uuid,
  "access_types" integer NOT NULL DEFAULT 0,
  "grant_types" integer NOT NULL DEFAULT 0,
  PRIMARY KEY ("entity_id", "principal_id")
);

CREATE TABLE "access_control"."principal" (
  "id" serial  PRIMARY KEY,
  "principal_id" uuid UNIQUE,
  "group_id" int
);

CREATE TABLE "access_control"."user" (
  "user_id" uuid PRIMARY KEY,
  "name"  text
);

CREATE TABLE "access_control"."group" (
  "group_id" uuid PRIMARY KEY,
  "name"  text,
  "description" text,
  "reserved" boolean  NOT NULL  DEFAULT false
);

ALTER TABLE "access_control"."entity_access" ADD FOREIGN KEY ("principal_id") REFERENCES "access_control"."principal" ("principal_id");
ALTER TABLE "access_control"."principal" ADD FOREIGN KEY ("group_id")  REFERENCES "access_control"."principal" ("id");


CREATE INDEX ON "access_control"."access_type" ("name");
CREATE INDEX ON "access_control"."entity_access" ("entity_id", "principal_id");
CREATE INDEX ON "access_control"."principal" ("principal_id");
CREATE INDEX ON "access_control"."principal" ("group_id");
CREATE INDEX ON "access_control"."user" ("user_id");
CREATE INDEX ON "access_control"."group" ("group_id");

COMMENT ON TABLE "access_control"."entity_access" IS 'Defines what access specific principals have to specific entities';
COMMENT ON TABLE "access_control"."access_type" IS 'Master data list of access types';
COMMENT ON TABLE "access_control"."principal" IS 'Self referential table of principals. Groups are also principals. The table links users to groups via this self-reference';

COMMENT ON COLUMN "access_control"."access_type"."mask" IS 'NONE=0  CREATE=1 READ=2,  UPDATE=4,  DELETE=8, ...';
COMMENT ON COLUMN "access_control"."entity_access"."access_types" IS 'Principal has these';
COMMENT ON COLUMN "access_control"."entity_access"."grant_types" IS 'Principal may grant these to others';







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


CREATE OR REPLACE FUNCTION access_control.fn_get_system_user() RETURNS uuid AS $$
BEGIN
	RETURN (SELECT user_id FROM access_control.user WHERE name = 'System');
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.fn_get_system_user IS 'The user named System is special as it is the only one created with the database. It is used to grant permissions';

CREATE OR REPLACE FUNCTION access_control.fn_get_audit_root_key() RETURNS uuid AS $$
BEGIN
  -- to keep someone from regenerating the entire audit trail from a known root we probably need a way to encrypt the first value
	RETURN uuid_generate_v4();
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.fn_get_audit_root_key IS 'Not yet sure how we will generate this value or manage it. This function is a way to abstract this indecision from the audit log implementation';

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
      INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Success', CURRENT_TIMESTAMP) RETURNING audit_id INTO a_id;
      --INSERT INTO audit.auditlink (asset_id, audit_id) VALUES (e, a_id); Audit kink must not have FK to asset - it must also use the entity abstraction asset types are not assets but they are entities
    ELSE
      msg := grantor::text ||' granted ' || access_mask || ' access of ' || e || ' to ' || for_principal;
      INSERT INTO audit.audit (audit_id, principal_id, action, status, event_time) VALUES (uuid_generate_v4(), for_principal, msg, 'Failed', CURRENT_TIMESTAMP) RETURNING audit_id INTO a_id;
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
COMMENT ON FUNCTION access_control.sp_grant_access IS 'Revoke the access of principal `for_principal` to all entities (e.g. asset_ids) in the list `to_entities uuid`';

CREATE OR REPLACE FUNCTION access_control.sp_add_group(code varchar(10), description text DEFAULT NULL)  RETURNS uuid AS $$
DECLARE
    group_id uuid;
BEGIN
  group_id := uuid_generate_v4();
	INSERT INTO access_control."group" (group_id, name, description) VALUES (group_id, code, description);
  INSERT INTO access_control.principal(principal_id) VALUES (group_id);
  
  RETURN group_id;
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.sp_grant_access IS 'Add a new Group';

CREATE OR REPLACE FUNCTION access_control.sp_remove_group(code varchar(10))  RETURNS uuid AS $$
DECLARE
    g_id uuid;
    user_count int;
BEGIN
  -- MUst add a way to move users to another group so we can delete this group
  g_id := (SELECT group_id FROM access_control.group WHERE name = code);
  user_count := (SELECT count(*) FROM access_control.principal WHERE group_id = (SELECT group_id FROM principal WHERE principal_id = g_id));
  IF EXISTS(SELECT reserved FROM access_control.group WHERE group_id = group_id AND reserved = true) THEN
    RAISE EXCEPTION 'Cannot delete reserved group %. ', code;
  END IF;
  
  IF (user_count > 0) THEN
    RAISE EXCEPTION 'Cannot delete group %. It has % users attached.', code, user_count;
  END IF;

  DELETE FROM access_control.entity_access WHERE principal_id = g_id;
  DELETE FROM access_control.principal WHERE principal_id = g_id;
  DELETE FROM access_control.group WHERE group_id = g_id;
	  
  RETURN user_id;
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.sp_remove_group IS 'Remove a Group. Not allowed to do so if there are still users attached.';


CREATE OR REPLACE FUNCTION access_control.sp_add_user(user_id uuid, code varchar(10), description text DEFAULT NULL)  RETURNS uuid AS $$
BEGIN
  IF UPPER(code) = 'SYSTEM' THEN
    RAISE EXCEPTION 'Cannot add user. % is a reserved name. %', code, e;
  END IF;
	INSERT INTO access_control."user" (user_id, name, description) VALUES (user_id, code, description);
  INSERT INTO access_control.principal(principal_id) VALUES (user_id);
  
  RETURN user_id;
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.sp_grant_access IS 'Add a new User. We must specify the uuid instead of the code as we need to use the indentity from Auth.';

CREATE OR REPLACE FUNCTION access_control.sp_remove_user(code varchar(10))  RETURNS uuid AS $$
DECLARE
    user_id uuid;
BEGIN
  IF UPPER(code) = 'SYSTEM' THEN
    RAISE EXCEPTION 'Cannot remove user. % is a reserved name. %', code, e;
  END IF;
  user_id := (SELECT user_id FROM access_control.user WHERE name = code);
  DELETE FROM access_control.entity_access WHERE principal_id = user_id;
  DELETE FROM access_control.principal WHERE principal_id = user_id;
  DELETE FROM access_control.user WHERE principal_id = user_id;
	  
  RETURN user_id;
END; $$ 
LANGUAGE PLPGSQL
SECURITY DEFINER
;
COMMENT ON FUNCTION access_control.sp_grant_access IS 'Remove a User';


CREATE OR REPLACE FUNCTION access_control.sp_add_user_to_group(user_id uuid, group_name varchar(10))  RETURNS void AS $$
DECLARE 
  g_id int;
BEGIN
  IF EXISTS(SELECT id FROM access_control.principal WHERE principal_id = user_id ) THEN
    g_id := (SELECT id FROM access_control.principal WHERE principal_id = (SELECT group_id FROM access_control.group WHERE name = group_name));
    IF g_id <> NULL THEN
      INSERT INTO access_control.principal (principal_id, group_id) VALUES (user_id, g_id);
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

CREATE OR REPLACE FUNCTION access_control.sp_add_remove_user_from_group(user_id uuid, group_name varchar(10))  RETURNS void AS $$
DECLARE 
  g_id int;
BEGIN
  IF EXISTS(SELECT id FROM access_control.principal WHERE principal_id = user_id ) THEN
    g_id := (SELECT id FROM access_control.principal WHERE principal_id = (SELECT group_id FROM access_control.group WHERE name = group_name));
    IF g_id <> NULL THEN
      DELETE FROM access_control.principal WHERE principal_id = user_id AND  group_id = g_id;
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

DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'normal_reader') THEN

      CREATE ROLE normal_reader LOGIN PASSWORD 'normal_reader';
   END IF;
END
$do$;

DELETE FROM access_control.access_type;
INSERT INTO access_control.access_type (mask, name) VALUES (0, 'NONE');
INSERT INTO access_control.access_type (mask, name) VALUES (1, 'CREATE');
INSERT INTO access_control.access_type (mask, name) VALUES (2, 'READ');
INSERT INTO access_control.access_type (mask, name) VALUES (4, 'UPDATE');
INSERT INTO access_control.access_type (mask, name) VALUES (8, 'DELETE');

-- Add a system user
INSERT INTO access_control.user (user_id,name) VALUES (uuid_generate_v4(), 'System');
INSERT INTO access_control.principal (principal_id) VALUES ((SELECT user_id FROM access_control.user WHERE name = 'System'));

--select * from information_schema.table_privileges where grantee = 'normal_reader'
--select * from information_schema.routine_privileges where grantee = 'normal_reader'

GRANT SELECT ON TABLE asset TO normal_reader;
GRANT SELECT ON TABLE assettype TO normal_reader;
GRANT SELECT ON TABLE location TO normal_reader;
GRANT SELECT ON TABLE asset_link TO normal_reader;
GRANT SELECT ON TABLE external_id_type TO normal_reader;

GRANT USAGE ON SCHEMA asset TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_envelope TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_landparcel TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_facility TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_building TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_site TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_floor TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_room TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_component TO normal_reader;

GRANT SELECT ON TABLE asset.ref_ward TO normal_reader;
GRANT SELECT ON TABLE asset.ref_suburb TO normal_reader;
GRANT SELECT ON TABLE asset.ref_region TO normal_reader;
GRANT SELECT ON TABLE asset.ref_town TO normal_reader;
GRANT SELECT ON TABLE asset.ref_municipality TO normal_reader;
GRANT SELECT ON TABLE asset.ref_district TO normal_reader;
GRANT SELECT ON TABLE asset.ref_facility_type TO normal_reader;

GRANT USAGE ON SCHEMA dtpw TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_client_department TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_chief_directorate TO normal_reader;

GRANT USAGE ON SCHEMA access_control TO normal_reader;
GRANT SELECT ON TABLE access_control.user TO normal_reader;
GRANT SELECT ON TABLE access_control.group TO normal_reader;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO normal_reader;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO normal_reader;


DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'importer') THEN

      CREATE ROLE importer LOGIN PASSWORD 'importer';
   END IF;
END
$do$;

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE assettype TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE location TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset_link TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE external_id_type TO importer;

GRANT USAGE ON SCHEMA asset TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_envelope TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_landparcel TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_facility TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_building TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_site TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_floor TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_room TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_component TO importer;

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_ward TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_suburb TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_region TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_town TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_municipality TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_district TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_facility_type TO importer;

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE unit TO importer;

GRANT USAGE ON SCHEMA dtpw TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_branch TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_client_department TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_chief_directorate TO importer;

GRANT USAGE ON SCHEMA access_control TO importer;
GRANT SELECT ON TABLE access_control.entity_access TO importer;
GRANT SELECT ON TABLE access_control.principal TO importer;
GRANT SELECT ON TABLE access_control.user TO importer;
GRANT SELECT ON TABLE access_control.group TO importer;
GRANT SELECT ON access_control.access_type TO importer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_grant_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_revoke_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_add_group TO importer;

GRANT USAGE ON SCHEMA transactions TO importer;
GRANT ALL ON TABLE transactions.transaction TO importer;
GRANT ALL ON TABLE transactions.transaction_type TO importer;
GRANT ALL ON TABLE transactions.field TO importer;