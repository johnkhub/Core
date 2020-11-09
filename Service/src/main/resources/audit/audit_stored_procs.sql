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
//
COMMENT ON FUNCTION "audit".fn_add_audit_row IS 'Trigger function that calculates the hash of the audit row (including that of previous row)';
//

CREATE TRIGGER on_audit_insert
BEFORE insert
ON audit.audit
FOR EACH ROW EXECUTE PROCEDURE audit.fn_add_audit_row();
//

CREATE OR REPLACE FUNCTION audit.log(
    principal_id uuid,
    event_time timestamp without time zone,
    action text,
    status text,
    parameters jsonb,
    entity_id uuid
) RETURNS void AS $$
DECLARE
    audit_id uuid;
BEGIN
    audit_id := uuid_generate_v4();
    INSERT INTO audit.audit (audit_id, principal_id, event_time, action, status, parameters)
    VALUES (audit_id, principal_id, event_time, action, status, parameters);

    IF (entity_id IS NOT NULL ) THEN
        INSERT INTO audit.auditlink (entity_id, audit_id) VALUES (entity_id, audit_id);
    END IF;
END; $$
    LANGUAGE PLPGSQL
    SECURITY DEFINER
;
//


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
