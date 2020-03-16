//
CREATE OR REPLACE VIEW asset.landparcel_view AS
SELECT
    a.*, p.lpi
FROM asset a JOIN asset.a_tp_landparcel p ON a.asset_id = p.asset_id;
//