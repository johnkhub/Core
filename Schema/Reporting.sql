/*
CREATE TABLE data_snapshots (
    id serial PRIMARY KEY,
    name text NOT NULL UNIQUE,
    last_update timestamp NOT NULL DEFAULT NOW(),
    update_time_ms bigint,
    is_refreshing boolean,
    owner uuid
);
*/

CREATE OR REPLACE VIEW public.tables_in_view_view AS
SELECT u.view_schema AS schema_name,
       u.view_name,
       u.table_schema AS referenced_table_schema,
       u.table_name AS referenced_table_name,
       v.view_definition
FROM information_schema.view_table_usage u
JOIN information_schema.views v ON u.view_schema = v.table_schema AND u.view_name = v.table_name
WHERE u.table_schema not in ('information_schema', 'pg_catalog')
ORDER BY u.view_schema, u.view_name;

COMMENT ON VIEW public.tables_in_view_view IS 'This view returns the names of all of the tables that are included in view definitions';


CREATE OR REPLACE VIEW public.index_definition_by_view_view AS
SELECT
	view_name,
    tablename,
    indexname,
    indexdef
FROM
    pg_indexes JOIN tables_in_view_view v ON pg_indexes.tablename = v.referenced_table_name
ORDER BY
	view_name,
    tablename,
    indexname;

COMMENT ON VIEW public.index_definition_by_view_view 
AS 'Gets the definitions of all of the indexes on all of the tables on all of the views. This can serve as a source of information when deciding on what indexes to create on materialized views.'