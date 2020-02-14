SELECT '========================== 13_Departments.sql ===========================';

ALTER TABLE asset_import ADD COLUMN  dept_code text;
UPDATE asset_import SET dept_code = (SELECT k FROM "dtpw"."ref_client_department" WHERE v = "Department");

INSERT INTO asset_classification(asset_id,responsible_dept_code)
SELECT asset_id, dept_code
FROM asset JOIN asset_import ON asset.func_loc_path <@ text2ltree("AssetID") AND dept_code IS NOT NULL;