-- TODO The community edition of liquibase does not support adding CHECK constraints, this is why I am adding them here
ALTER TABLE dtpw.ref_branch DROP CONSTRAINT IF EXISTS ref_branch_k_check;
ALTER TABLE dtpw.ref_branch ADD CONSTRAINT ref_branch_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);

ALTER TABLE dtpw.ref_chief_directorate DROP CONSTRAINT IF EXISTS ref_chief_directorate_k_check;
ALTER TABLE dtpw.ref_chief_directorate ADD CONSTRAINT ref_chief_directorate_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);

ALTER TABLE dtpw.ref_client_department DROP CONSTRAINT IF EXISTS ref_client_department_k_check;
ALTER TABLE dtpw.ref_client_department ADD CONSTRAINT ref_client_department_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);

ALTER TABLE dtpw.ref_ei_district DROP CONSTRAINT IF EXISTS ref_ei_district_k_check;
ALTER TABLE dtpw.ref_ei_district ADD CONSTRAINT ref_ei_district_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);


ALTER TABLE dtpw.ref_deed_office ADD CONSTRAINT ref_deed_office_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE dtpw.ref_land_use_class ADD CONSTRAINT ref_land_use_class_k_check  CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE dtpw.ref_accommodation_type ADD CONSTRAINT ref_accommodation_type_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);