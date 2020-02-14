CREATE SCHEMA "audit";

CREATE TABLE "audit"."audit" (
  "audit_id" uuid PRIMARY KEY,
  "principal_id" uuid NOT NULL,
  "insert_time" timestamp NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  "event_time" timestamp NOT NULL,
  "action" varchar NOT NULL,
  "status" varchar NOT NULL,
  "tamper_check" varchar NOT NULL
);

CREATE TABLE "audit"."auditlink" (
  "asset_id" uuid,
  "audit_id" uuid UNIQUE,
  PRIMARY KEY ("asset_id", "audit_id")
);

ALTER TABLE "audit"."auditlink" ADD FOREIGN KEY ("audit_id") REFERENCES "audit"."audit" ("audit_id");
ALTER TABLE "audit"."auditlink" ADD FOREIGN KEY ("asset_id") REFERENCES "asset" ("asset_id");

CREATE INDEX ON "audit"."audit" ("audit_id");
CREATE INDEX ON "audit"."audit" ("insert_time");
CREATE INDEX ON "audit"."audit" ("event_time", "principal_id");
CREATE INDEX ON "audit"."audit" ("event_time", "action", "status");
CREATE INDEX ON "audit"."auditlink" ("asset_id", "audit_id");

COMMENT ON COLUMN "audit"."audit"."principal_id" IS 'Principal that posted Transaction';
COMMENT ON COLUMN "audit"."audit"."action" IS 'Action like create, delete, login etc.';
COMMENT ON COLUMN "audit"."audit"."status" IS 'Succeeded, failed etc.';

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

