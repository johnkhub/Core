INSERT INTO kv_type (code,name,"table") VALUES ('WARD', 'Ward', 'public.ref_ward') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('REGION', 'Region', 'public.ref_region') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('TOWN', 'Town', 'public.ref_town') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('MUNIC', 'Municipality', 'public.ref_municipality') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('DISTRICT', 'District', 'public.ref_district') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('SUBURB', 'Suburb', 'public.ref_suburb') ON CONFLICT (code) DO NOTHING;

INSERT INTO kv_type (code,name,"table") VALUES ('TAGS', 'List of tags', 'public.tags') ON CONFLICT (code) DO NOTHING;