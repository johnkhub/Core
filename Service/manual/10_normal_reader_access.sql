DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'normal_reader') THEN

      CREATE ROLE normal_reader LOGIN PASSWORD 'normal_reader';
   END IF;
END
$do$;


--select * from information_schema.table_privileges where grantee = 'normal_reader'
--select * from information_schema.routine_privileges where grantee = 'normal_reader'

--
-- public
--

GRANT SELECT ON TABLE asset TO normal_reader;
GRANT SELECT ON TABLE asset_classification TO normal_reader;
GRANT SELECT ON TABLE asset_identification TO normal_reader;
GRANT SELECT ON TABLE asset_link TO normal_reader;
GRANT SELECT ON TABLE asset_grouping TO normal_reader;
GRANT SELECT ON TABLE asset_tags TO normal_reader;
GRANT SELECT ON TABLE assettype TO normal_reader;
GRANT SELECT ON TABLE external_id_type TO normal_reader;
GRANT SELECT ON TABLE grouping_id_type TO normal_reader;

GRANT SELECT ON TABLE finyear TO normal_reader;

GRANT SELECT ON TABLE geoms TO normal_reader;

GRANT SELECT ON TABLE kv_base TO normal_reader;
GRANT SELECT ON TABLE kv_type TO normal_reader;

GRANT SELECT ON TABLE location TO normal_reader;

GRANT SELECT ON TABLE postal_code TO normal_reader;

GRANT SELECT ON TABLE tags TO normal_reader;
GRANT SELECT ON TABLE unit TO normal_reader;


-- NO! GRANT EXECUTE ON FUNCTION fn_add_tags TO normal_reader;
GRANT EXECUTE ON FUNCTION fn_has_tags TO normal_reader;

GRANT SELECT ON asset_core_view TO normal_reader;


--
-- asset
--
GRANT USAGE ON SCHEMA asset TO normal_reader;

GRANT SELECT ON TABLE asset.a_tp_building TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_component TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_envelope TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_facility TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_floor TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_landparcel TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_room TO normal_reader;
GRANT SELECT ON TABLE asset.a_tp_site TO normal_reader;

GRANT SELECT ON TABLE asset.asset_landparcel TO normal_reader;

GRANT SELECT ON TABLE public.ref_district TO normal_reader;
GRANT SELECT ON TABLE asset.ref_facility_type TO normal_reader;
GRANT SELECT ON TABLE public.ref_municipality TO normal_reader;
GRANT SELECT ON TABLE public.ref_region TO normal_reader;
GRANT SELECT ON TABLE public.ref_suburb TO normal_reader;
GRANT SELECT ON TABLE public.ref_town TO normal_reader;
GRANT SELECT ON TABLE public.ref_ward TO normal_reader;

GRANT SELECT ON  asset.landparcel_view TO normal_reader;


--
-- audit
--
GRANT USAGE ON SCHEMA audit TO normal_reader;
GRANT SELECT ON TABLE audit.audit TO normal_reader;
GRANT SELECT ON TABLE audit.audit_type TO normal_reader;
GRANT SELECT ON TABLE audit.auditlink TO normal_reader;


--
-- dtpw
--
GRANT USAGE ON SCHEMA dtpw TO normal_reader;

GRANT SELECT ON TABLE dtpw.ref_branch TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_chief_directorate TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_client_department TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_ei_district TO normal_reader;
GRANT SELECT ON TABLE dtpw.ei_district_link TO normal_reader;

GRANT SELECT ON  dtpw.dtpw_core_report_view TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view_with_lpi  TO normal_reader;

GRANT SELECT ON  dtpw.asset_core_dtpw_ei_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_gi_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_hi_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_rnm_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_iam_view  TO normal_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_ppp_view  TO normal_reader;

GRANT SELECT ON  dtpw.dtpw_core_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_ei_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_gi_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_hi_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_iam_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_rnm_report_view_wrapper  TO normal_reader;
GRANT SELECT ON  dtpw.dtpw_ppp_report_view_wrapper  TO normal_reader;

GRANT SELECT ON  dtpw.dtpw_export_view  TO normal_reader;;







--
-- access control
--
GRANT USAGE ON SCHEMA access_control TO normal_reader;

GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO normal_reader;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO normal_reader;

-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_add_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_add_user_to_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_grant_access TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user_from_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_revoke_access TO importer;



SELECT all_views.* FROM
    (
        SELECT
            grantee, table_schema, table_name
        FROM Information_schema.table_privileges
        WHERE
            privilege_type = 'SELECT' AND
            grantee = 'postgres' AND
            table_name IN
            (
                SELECT
                    table_name AS view_name
                FROM INformation_schema.views
                WHERE table_schema NOT IN ('information_schema', 'pg_catalog','public')
                ORDER BY view_name
            )
    )  AS all_views -- User postgres owns all views
LEFT JOIN
    (
        SELECT
            grantee, table_schema, table_name
        FROM INformation_schema.table_privileges
        WHERE
            privilege_type = 'SELECT' AND
            grantee = 'normal_reader' AND
            table_name IN
            (
                SELECT
                    table_name AS view_name
                FROM INformation_schema.views
                WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'public')
                ORDER BY view_name
            )
    ) AS reader -- User normal_reader needs to be granted read access
ON all_views.table_name = reader.table_name
WHERE reader.table_name is null
