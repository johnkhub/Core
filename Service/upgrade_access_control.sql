
DROP TABLE access_control.entity_access;
DROP TABLE access_control.access_type;
DROP TABLE access_control.principal;

CREATE TABLE access_control.entity_access (
	entity_id uuid NOT NULL,
	principal_id uuid NOT NULL,
	access_types int4 NOT NULL DEFAULT 0,
	grant_types int4 NOT NULL DEFAULT 0,
	CONSTRAINT entity_access_pkey PRIMARY KEY (entity_id, principal_id)
);

CREATE TABLE access_control.access_type (
	"name" varchar(10) NOT NULL,
	mask int4 NOT NULL,
	CONSTRAINT access_type_pkey PRIMARY KEY (name)
);

CREATE TABLE access_control.principal (
	id serial NOT NULL,
	principal_id uuid NULL,
	group_id int4 NULL,
	"name" text NOT NULL,
	description text NULL,
	is_group bool NOT NULL,
	reserved bool NOT NULL DEFAULT false,
	CONSTRAINT principal_name_key UNIQUE (name),
	CONSTRAINT principal_pkey PRIMARY KEY (id),
	CONSTRAINT principal_principal_id_key UNIQUE (principal_id),
	CONSTRAINT principal_group_id_fkey FOREIGN KEY (group_id) REFERENCES access_control.principal(id)
);

CREATE INDEX principal_group_id_idx ON access_control.principal USING btree (group_id);
CREATE INDEX principal_principal_id_idx ON access_control.principal USING btree (principal_id);

ALTER TABLE access_control.entity_access ADD CONSTRAINT entity_access_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES access_control.principal(principal_id);