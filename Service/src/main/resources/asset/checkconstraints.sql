-- TODO The community edition of liquibase does not support adding CHECK constraints, this is why I am adding them here
ALTER TABLE asset.ref_district ADD CONSTRAINT ref_district_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_facility_type ADD CONSTRAINT ref_facility_type_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_municipality ADD CONSTRAINT ref_municipality_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_region ADD CONSTRAINT ref_region_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_suburb ADD CONSTRAINT ref_suburb_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_town ADD CONSTRAINT ref_town_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE asset.ref_ward ADD CONSTRAINT ref_ward_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);