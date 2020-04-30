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
