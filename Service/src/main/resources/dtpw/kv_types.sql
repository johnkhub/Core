INSERT INTO kv_type (code,name,"table") VALUES ('BRANCH', 'Branch', 'dtpw.ref_branch' ) ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('CHIEF_DIR', 'Chief Directorate', 'dtpw.ref_chief_directorate' ) ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('CLIENT_DEP', 'Client Department', 'dtpw.ref_client_department') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('EI_DISTR', 'Educational District', 'dtpw.ref_ei_district') ON CONFLICT (code) DO NOTHING;


INSERT INTO kv_type (code,name,"table") VALUES ('DEED_OFFICE', 'Deed Office', 'dtpw.ref_deed_office') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('LAND_USE_CLASS', 'Land use class', 'dtpw.ref_land_use_class') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('ACCOMODATION_TYPE', 'Accomodation Type', 'dtpw.ref_accommodation_type') ON CONFLICT (code) DO NOTHING;

