-- TODO The community edition of liquibase does not support adding CHECK constraints, this is why I am adding them here
ALTER TABLE dtpw.ref_branch ADD CONSTRAINT ref_branch_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE dtpw.ref_chief_directorate ADD CONSTRAINT ref_chief_directorate_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE dtpw.ref_client_department ADD CONSTRAINT ref_client_department_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE dtpw.ref_ei_district ADD CONSTRAINT ref_ei_district_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);


