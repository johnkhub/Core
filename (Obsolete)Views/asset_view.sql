CREATE OR REPLACE active_asset_view AS
SELECT 
    asset_id, asset_type_code, code, name, adm_path, func_loc_path, grap_path
FROM 
    asset
    JOIN entity_access ON (entity_id = asset_id) AND fn_get_effective_access((SELECT user_id FROM "user" WHERE name = 'Piet'), asset.asset_id) & 2 = 2
WHERE deactivated_at IS NULL;