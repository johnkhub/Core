UPDATE dtpw.ref_ei_district SET v = trim(v);

CREATE TABLE ei_district_import (
                                    "Emis" text,
                                    "Education District" text,
                                    asset_id uuid,
                                    code text
);

-- IMPORT FROM CSV HERE `Emis` and `code`

UPDATE  ei_district_import  SET  "Education District" = 'Eden and Central Karoo' WHERE  "Education District" = 'Central Karoo';

UPDATE ei_district_import
SET code = (SELECT k FROM dtpw.ref_ei_district WHERE v = upper("Education District"));

INSERT INTO dtpw.ei_district_link (asset_id, k_education_district)
SELECT
    l.asset_id,
    i.code
FROM asset_link l JOIN ei_district_import i ON i."Emis" = l.external_id

select * from dtpw.dtpw_export_view order by func_loc_path asc

