UPDATE kv_type SET name = 'Municipality' WHERE code = 'MUNIC';

UPDATE asset SET func_loc_path = '5922.0010' WHERE code = '5922-0010';
UPDATE asset SET func_loc_path = '5922.0001' WHERE code = '5922-0001';
UPDATE asset SET func_loc_path = '5922.0000' WHERE code = '5922-0000';

UPDATE asset SET func_loc_path = '5922.5922.LF02' WHERE code = '5922-5922-LF02';
UPDATE asset SET func_loc_path = '5922.5922.LF03' WHERE code = '5922-5922-LF03';
UPDATE asset SET func_loc_path = '5922.5922.LF04' WHERE code = '5922-5922-LF04';
UPDATE asset SET func_loc_path = '5922.5922.LF05' WHERE code = '5922-5922-LF05';
UPDATE asset SET func_loc_path = '5922.5922.LF06' WHERE code = '5922-5922-LF06';
UPDATE asset SET func_loc_path = '5922.5922.LF07' WHERE code = '5922-5922-LF07';
UPDATE asset SET func_loc_path = '5922.5922.LF08' WHERE code = '5922-5922-LF08';
UPDATE asset SET func_loc_path = '5922.5922.LF11' WHERE code = '5922-5922-LF11';

DELETE FROM public.asset_grouping WHERE asset_id = '8308d142-27a1-409d-b43f-a28499b1eba7' and grouping_id != '102480835';
DELETE FROM public.asset_grouping WHERE asset_id = '23af4760-afed-4333-b559-f1c81fc1cae9' and grouping_id != '102483567';
