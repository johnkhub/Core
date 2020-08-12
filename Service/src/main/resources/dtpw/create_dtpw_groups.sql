-- Create groups for DTPW Brances, Chief Directorates and Departments
DO $$
DECLARE 
	cur_group CURSOR FOR SELECT * FROM dtpw.ref_branch;
	g RECORD;
BEGIN
	OPEN cur_group;
	
	LOOP
		FETCH cur_group INTO g;
		EXIT WHEN NOT FOUND;
		
		PERFORM (access_control.sp_remove_group(g.k));
		PERFORM (access_control.sp_add_group(g.k, g.v));
	END LOOP;

	CLOSE cur_group;
END $$ LANGUAGE plpgsql;

//

DO $$
DECLARE 
	cur_group CURSOR FOR SELECT * FROM dtpw.ref_chief_directorate;
	g RECORD;
BEGIN
	OPEN cur_group;
	
	LOOP
		FETCH cur_group INTO g;
		EXIT WHEN NOT FOUND;
		
		PERFORM (access_control.sp_remove_group(g.k));
		PERFORM (access_control.sp_add_group(g.k, g.v));
	END LOOP;

	CLOSE cur_group;
END $$ LANGUAGE plpgsql;

//

DO $$
DECLARE 
	cur_group CURSOR FOR SELECT * FROM dtpw.ref_client_department;
	g RECORD;
BEGIN
	OPEN cur_group;
	
	LOOP
		FETCH cur_group INTO g;
		EXIT WHEN NOT FOUND;
		
		PERFORM (access_control.sp_remove_group(g.k));
		PERFORM (access_control.sp_add_group(g.k, g.v));
	END LOOP;

	CLOSE cur_group;
END $$ LANGUAGE plpgsql;

//

-- For the Create permission we actually need to tie the permission to the type of asset rather than the asset instance. To achieve this, we have a uuid for each asset
-- type and insert that into the  entity_access table.

SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_HI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_PPP'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_IAM'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_GI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_EI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT principal_id FROM access_control.principal WHERE "name" = 'CD_RNM'));

//