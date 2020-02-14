
DELETE FROM entity_access;

SELECT sp_grant_access( -- grant Readers, Read access to all entities
	2, 
	ARRAY(SELECT asset_id FROM asset), 
	(SELECT group_id FROM "group" WHERE NAME = 'Readers')
);


SELECT * FROM asset WHERE name LIKE 'Groote%' AND  asset_type_code = 'ENVELOPE';

SELECT * FROM 
	asset 
	JOIN access_control.entity_access ON (entity_id = asset_id) AND  access_control.fn_get_effective_access((SELECT user_id FROM "user" WHERE name = 'Piet'), entity_id) & 2 = 2
WHERE func_loc_path <@ '2876' AND asset_type_code = 'BUILDING';