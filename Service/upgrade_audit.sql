
DROP TABLE audit.auditlink;
DROP TABLE audit.audit;

CREATE TABLE audit.auditlink (
	entity_id uuid NOT NULL,
	audit_id uuid NOT NULL,
	CONSTRAINT auditlink_audit_id_key UNIQUE (audit_id),
	CONSTRAINT auditlink_pkey PRIMARY KEY (entity_id, audit_id)
);

ALTER TABLE audit.auditlink ADD CONSTRAINT auditlink_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES audit.audit(audit_id);


CREATE TABLE audit.audit (
	audit_id uuid NOT NULL,
	principal_id uuid NOT NULL,
	insert_time timestamp NOT NULL DEFAULT now(),
	event_time timestamp NOT NULL,
	"action" varchar NOT NULL,
	status varchar NOT NULL,
	tamper_check varchar NOT NULL,
	parameters jsonb NOT NULL DEFAULT '{}'::jsonb,
	CONSTRAINT audit_pkey PRIMARY KEY (audit_id)
);
CREATE INDEX audit_event_time_action_status_idx ON audit.audit USING btree (event_time, action, status);
CREATE INDEX audit_event_time_principal_id_idx ON audit.audit USING btree (event_time, principal_id);
CREATE INDEX audit_insert_time_idx ON audit.audit USING btree (insert_time);
CREATE INDEX idx_action ON audit.audit USING btree (action);


ALTER TABLE audit.audit ADD CONSTRAINT audit_audit_type_fkey FOREIGN KEY (action) REFERENCES audit.audit_type(mnemonic);
CREATE TABLE audit.audit_type (
	mnemonic varchar(20) NOT NULL,
	description text NULL,
	meta jsonb NOT NULL DEFAULT '{}'::jsonb,
	CONSTRAINT "PK_AUDIT_TYPE" PRIMARY KEY (mnemonic)
);