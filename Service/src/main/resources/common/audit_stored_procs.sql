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