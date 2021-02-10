CREATE OR REPLACE VIEW public.imqs_meta_column_comment_view AS
SELECT
    c.table_schema,
    c.table_name,
    c.column_name,
    pgd.description
FROM pg_catalog.pg_statio_all_tables as st
         JOIN pg_catalog.pg_description pgd on (pgd.objoid=st.relid)
         JOIN information_schema.columns c on (pgd.objsubid=c.ordinal_position AND  c.table_schema=st.schemaname AND c.table_name=st.relname)
WHERE c.table_schema::text <> ALL (ARRAY['pg_catalog'::character varying, 'information_schema'::character varying]::text[]);

CREATE OR REPLACE VIEW public.imqs_meta_column_definition_view
AS
SELECT c.table_schema,
       c.table_name,
       c.column_name,
       CASE c.data_type
           WHEN 'USER-DEFINED'::text THEN c.udt_name::character varying
           WHEN 'character varying'::text THEN ('character varying'::text || COALESCE(('('::text || c.character_maximum_length) || ')'::text, ''::text))::character varying
           WHEN 'numeric'::text THEN (((('numeric('::text || c.numeric_precision) || ','::text) || c.numeric_scale) || ')'::text)::character varying
           ELSE c.data_type::character varying
           END AS type,
       c.column_default,
       c.is_nullable
FROM information_schema.columns c
WHERE c.table_schema::text <> ALL (ARRAY['pg_catalog'::character varying, 'information_schema'::character varying]::text[]);



CREATE OR REPLACE VIEW public.imqs_meta_column_constraint_view
AS
SELECT c.table_schema,
       c.table_name,
       c.column_name,
       r.constraint_name,
       chk.check_clause
FROM information_schema.columns c
         LEFT JOIN information_schema.constraint_column_usage r ON r.table_schema::text = c.table_schema::text AND r.table_name::text = c.table_name::text AND r.column_name::text = c.column_name::text
         LEFT JOIN information_schema.check_constraints chk ON chk.constraint_name::text = r.constraint_name::text
WHERE c.table_schema::text <> ALL (ARRAY['pg_catalog'::character varying, 'information_schema'::character varying]::text[]);





CREATE OR REPLACE VIEW imqs_meta_view_column_def_view AS
SELECT
    v.view_schema||'.'||v.view_name AS "fqn_view",
    v.view_schema,
    v.view_name,

    def.table_schema||'.'||def.table_name||'.'||def.column_name AS "fqn_column",

    def.table_schema,
    def.table_name,
    def.column_name,
    tabs.table_type,

    def.type,
    def.column_default,
    def.is_nullable,

    cons.check_clause,
    com.description

FROM
    information_schema.view_column_usage v
        INNER JOIN
    public.imqs_meta_column_definition_view def ON (def.table_schema = v.table_schema AND def.table_name = v.table_name AND def.column_name = v.column_name)
        INNER JOIN
    information_schema.tables tabs ON (def.table_schema = tabs.table_schema AND def.table_name = tabs.table_name)
        LEFT JOIN
    public.imqs_meta_column_constraint_view cons ON (def.table_schema = cons.table_schema AND def.table_name = cons.table_name AND def.column_name = cons.table_name)
        LEFT
            JOIN public.imqs_meta_column_comment_view com ON (def.table_schema = com.table_schema AND def.table_name = com.table_name AND def.column_name = com.table_name)
;
