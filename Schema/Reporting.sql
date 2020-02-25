CREATE TABLE data_snapshots (
    id serial PRIMARY KEY,
    name text NOT NULL UNIQUE,
    last_update timestamp NOT NULL DEFAULT NOW(),
    update_time_ms bigint,
    is_refreshing boolean,
    owner uuid
);

CREATE MATERIALIZED VIEW asset_core_dtpw_view_materialized AS 
SELECT * FROM asset_core_dtpw_view;

CREATE UNIQUE INDEX m1_asset_id_idx ON public.asset_core_dtpw_view_materialized USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON public.asset_core_dtpw_view_materialized USING gist (func_loc_path);
CREATE INDEX m1_geom_idx  ON public.asset_core_dtpw_view_materialized USING gist (geom);

CREATE INDEX m1_district_code_idx ON public.asset_core_dtpw_view_materialized USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON public.asset_core_dtpw_view_materialized USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON public.asset_core_dtpw_view_materialized USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON public.asset_core_dtpw_view_materialized USING btree (town_code);
CREATE UNIQUE INDEX "m1_EMIS_idx" ON public.asset_core_dtpw_view_materialized USING btree ("EMIS");
CREATE INDEX m1_responsible_dept_code_idx ON public.asset_core_dtpw_view_materialized USING btree (responsible_dept_code);


REFRESH MATERIALIZED VIEW asset_core_dtpw_view_materialized;
	

  
select * from asset_core_dtpw_view_materialized limit 10000;


--
-- This view returns the names of all of the tables that are included in view definitions
--
CREATE OR REPLACE VIEW tables_in_view_view AS
SELECT u.view_schema AS schema_name,
       u.view_name,
       u.table_schema AS referenced_table_schema,
       u.table_name AS referenced_table_name,
       v.view_definition
FROM information_schema.view_table_usage u
JOIN information_schema.views v ON u.view_schema = v.table_schema AND u.view_name = v.table_name
WHERE u.table_schema not in ('information_schema', 'pg_catalog')
ORDER BY u.view_schema, u.view_name;


-- 
--  Gets the definitions of all of the indexes on all of the tables on all of the views.
--  This can serve as a source of information when deciding on what indexes to create on
--  materialized views
--
SELECT
	view_name,
    tablename,
    indexname,
    indexdef
FROM
    pg_indexes JOIN tables_in_view_view v ON pg_indexes.tablename = v.referenced_table_name

WHERE
    schemaname = 'public'
ORDER BY
	view_name,
    tablename,
    indexname;