CREATE SCHEMA dtpw;

CREATE TABLE dtpw.ref_branch (
) INHERITS (kv_base);

CREATE TABLE dtpw.ref_client_department (
  chief_directorate_code varchar(10),
  "responsible_dept_classif" ltree NULL
) INHERITS (kv_base);

CREATE INDEX "responsible_dept_classif" ON "dtpw"."ref_client_department"  USING gist ("responsible_dept_classif");
COMMENT ON COLUMN "dtpw"."ref_client_department"."responsible_dept_classif" IS '<branch>.<chief directorate>.<reponsible department>';

CREATE TABLE dtpw.ref_chief_directorate (
  branch_code varchar(10)
) INHERITS (kv_base);

CREATE UNIQUE INDEX ON "dtpw"."ref_branch" ("k");
CREATE UNIQUE INDEX ON "dtpw"."ref_client_department" ("k");
CREATE UNIQUE INDEX ON "dtpw"."ref_chief_directorate" ("k");
CREATE UNIQUE INDEX ON "dtpw"."ref_branch" ("v");
CREATE UNIQUE INDEX ON "dtpw"."ref_client_department" ("v");
CREATE UNIQUE INDEX ON "dtpw"."ref_chief_directorate" ("v");

INSERT INTO kv_type (code,name,"table") VALUES ('BRANCH', 'Branch', 'dtpw.ref_branch' );
INSERT INTO kv_type (code,name,"table") VALUES ('CHIEF_DIR', 'Chief Directorate', 'dtpw.ref_chief_directorate' );
INSERT INTO kv_type (code,name,"table") VALUES ('CLIENT_DEP', 'Client Department', 'dtpw.ref_client_department');