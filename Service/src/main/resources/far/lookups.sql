
-- refComponent_DepreciationMethod --
INSERT INTO public."ref_depreciation_method" (k, v) VALUES ('STR', 'Straight Line') ON CONFLICT(k) DO NOTHING;
INSERT INTO public."ref_depreciation_method" (k, v) VALUES ('DEC', 'Declining Balance (10%)') ON CONFLICT(k) DO NOTHING;

-- ref_disposal_method --
DELETE FROM "ref_disposal_method";
INSERT INTO "ref_disposal_method" (k,v) VALUES ('ALIN','Alienated') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('DEC','Decommissioned') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('DEST','Destroyed') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('DISP','Disposed') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('LOST','Lost') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('NA','Not Applicable') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('SOL','Sold') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('STOL','Stolen') ON CONFLICT(k) DO NOTHING;
INSERT INTO "ref_disposal_method" (k,v) VALUES ('TRF','Transfer') ON CONFLICT(k) DO NOTHING;

-- ref_measurement_mode --
DELETE FROM public."ref_measurement_mode" WHERE k IN ('FV','HC','RE');
INSERT INTO public."ref_measurement_mode" (k, v) VALUES ('FV', 'Fair Value') ON CONFLICT(k) DO NOTHING;
INSERT INTO public."ref_measurement_mode" (k, v) VALUES ('HC', 'Historical Cost') ON CONFLICT(k) DO NOTHING;
INSERT INTO public."ref_measurement_mode" (k, v) VALUES ('RE', 'Revaluation') ON CONFLICT(k) DO NOTHING;



