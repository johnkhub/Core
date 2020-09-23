ALTER TABLE dtpw.ref_ei_district ADD CONSTRAINT ref_ei_district_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);

