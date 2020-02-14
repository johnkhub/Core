DELETE FROM asset_link WHERE external_id_type = '4a6a4f78-2dc4-4b29-aa9e-5033b834a564';
INSERT INTO asset_link (asset_id, external_id, external_id_type) 
SELECT 
    (SELECT (asset_id) FROM asset WHERE code = "Code (uk)") AS asset_id,
    "EMIS" AS external_id,
    '4a6a4f78-2dc4-4b29-aa9e-5033b834a564' AS external_type_id
FROM asset_import WHERE "EMIS" IS NOT NULL;