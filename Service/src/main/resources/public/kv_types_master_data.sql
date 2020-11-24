INSERT INTO kv_type (code,name,"table") VALUES ('WARD', 'Ward', 'public.ref_ward') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('REGION', 'Region', 'public.ref_region') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('TOWN', 'Town', 'public.ref_town') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('MUNIC', 'Municipality', 'public.ref_municipality') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('DISTRICT', 'District', 'public.ref_district') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('SUBURB', 'Suburb', 'public.ref_suburb') ON CONFLICT (code) DO NOTHING;

INSERT INTO kv_type (code,name,"table") VALUES ('TAGS', 'List of tags', 'public.tags') ON CONFLICT (code) DO NOTHING;




INSERT INTO kv_type (code,name,"table") VALUES ('ASSET_CLASS', 'Asset Class', 'public.ref_asset_class') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('ASSET_NATURE', 'Asset Nature', 'public.ref_asset_nature') ON CONFLICT (code) DO NOTHING;

INSERT INTO kv_type (code,name,"table") VALUES ('CRITICALITY_RATING', 'Criticality Rating', 'public.ref_criticality_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('UTILISATION_RATING', 'Utilisation Rating', 'public.ref_utilisation_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('ACCESSIBILITY_RATING', 'Accessibility Rating', 'public.ref_accessibility_rating') ON CONFLICT (code) DO NOTHING;

-- Non-standard KV
INSERT INTO kv_type (code,name,"table") VALUES ('CONFIDENCE_RATING', 'Data Confidence Rating', 'public.ref_confidence_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('CONDITION_RATING', 'Condition Rating', 'public.ref_condition_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('PERFORMANCE_RATING', 'Performance Rating', 'public.ref_performance_rating') ON CONFLICT (code) DO NOTHING;
