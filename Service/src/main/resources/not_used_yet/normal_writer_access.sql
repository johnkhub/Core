--
-- This is the user writing services should normally use
--

DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'normal_writer') THEN

      CREATE ROLE normal_writer LOGIN PASSWORD 'normal_writer';
   END IF;
END
$do$;

--select * from information_schema.table_privileges where grantee = 'normal_writer'
--select * from information_schema.routine_privileges where grantee = 'normal_writer'

--
-- public
--

GRANT SELECT,INSERT,UPDATE ON TABLE asset TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_classification TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_identification TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_link TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_tags TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE assettype TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE external_id_type TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE finyear TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE geoms TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE kv_base TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE kv_type TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE location TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE postal_code TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE tags TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE unit TO normal_writer;

GRANT EXECUTE ON FUNCTION fn_add_tags TO normal_writer;
GRANT EXECUTE ON FUNCTION fn_has_tags TO normal_writer;

GRANT SELECT ON VIEW asset_core_view TO normal_writer;

--
-- asset
--
GRANT USAGE ON SCHEMA asset TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_building TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_component TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_envelope TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_facility TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_floor TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_landparcel TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_room TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_site TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE asset.asset_landparcel TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_district TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_facility_type TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_municipality TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_region TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_suburb TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_town TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_ward TO normal_writer;

GRANT SELECT ON VIEW asset.landparcel_view TO normal_writer;

--
-- audit
--
GRANT USAGE ON SCHEMA audit TO normal_writer;

-- *** Note not update ***
GRANT SELECT,INSERT ON TABLE audit.audit TO normal_writer;
GRANT SELECT,INSERT ON TABLE audit.audit_type TO normal_writer;
GRANT SELECT,INSERT ON TABLE audit.auditlink TO normal_writer;

--
-- dtpw
--
GRANT USAGE ON SCHEMA dtpw TO normal_writer;

GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_branch TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_chief_directorate TO normal_writer;
GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_client_department TO normal_writer;

GRANT SELECT ON VIEW  dtpw.dtpw_core_report_view TO normal_writer;
GRANT SELECT ON VIEW  dtpw.asset_core_dtpw_view  TO normal_writer;
GRANT SELECT ON VIEW  dtpw.asset_core_dtpw_view_with_lpi  TO normal_writer;

--
-- access control
--
GRANT USAGE ON SCHEMA access_control TO normal_writer;

GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO normal_writer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO normal_writer;

--  *** Require a specific user to manage permission assignments ***
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_add_group TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_add_user_to_group TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_grant_access TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_group TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user_from_group TO normal_writer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_revoke_access TO normal_writer;

