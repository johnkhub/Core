-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

-- noinspection SyntaxErrorForFile

--
-- This is the user we use fro imports. It has more access than a normal_writer, but is still not allowed
-- to make destructive changes
--

DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'importer') THEN

      CREATE ROLE importer LOGIN PASSWORD 'importer';
   END IF;
END
$do$;
//
--select * from information_schema.table_privileges where grantee = 'importer'
--select * from information_schema.routine_privileges where grantee = 'importer'

--
-- public
--

GRANT SELECT,INSERT,UPDATE ON TABLE asset TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_classification TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_identification TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_link TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset_tags TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE assettype TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE external_id_type TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE finyear TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE geoms TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE kv_base TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE kv_type TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE location TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE postal_code TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE tags TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE unit TO importer;
//
GRANT EXECUTE ON FUNCTION fn_add_tags TO importer;
GRANT EXECUTE ON FUNCTION fn_has_tags TO importer;
//
GRANT SELECT ON asset_core_view TO importer;
//

-- *********************
-- **** DANGER ZONE ****
-- *********************
GRANT DELETE ON TABLE asset_link TO importer;
GRANT DELETE ON TABLE postal_code TO importer;
GRANT USAGE, SELECT ON SEQUENCE postal_code_id_seq TO importer;
-- *********************
--
-- asset
--
GRANT USAGE ON SCHEMA asset TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_building TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_component TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_envelope TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_facility TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_floor TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_landparcel TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_room TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_site TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE asset.asset_landparcel TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_district TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_facility_type TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_municipality TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_region TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_suburb TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_town TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.ref_ward TO importer;
//
GRANT SELECT ON  asset.landparcel_view TO importer;
//

-- *********************
-- **** DANGER ZONE ****
-- *********************
 GRANT DELETE ON TABLE asset_classification TO importer;
//
 GRANT DELETE ON TABLE asset.a_tp_envelope TO importer;
 GRANT DELETE ON TABLE asset.a_tp_building TO importer;
 GRANT DELETE ON TABLE asset.a_tp_component TO importer;
 GRANT DELETE ON TABLE asset.a_tp_facility TO importer;
 GRANT DELETE ON TABLE asset.a_tp_floor TO importer;
 GRANT DELETE ON TABLE asset.a_tp_room TO importer;
 GRANT DELETE ON TABLE asset.a_tp_site TO importer;
 GRANT DELETE ON TABLE asset.ref_suburb TO importer;
 GRANT DELETE ON TABLE asset.ref_district TO importer;
 GRANT DELETE ON TABLE asset.ref_town TO importer;
 GRANT DELETE ON TABLE asset.ref_municipality TO importer;
//

GRANT DELETE ON TABLE "asset"."asset_landparcel" TO importer;
GRANT DELETE ON TABLE asset.a_tp_landparcel TO importer;
GRANT DELETE ON TABLE asset.ref_district TO importer;
GRANT DELETE ON TABLE asset.ref_facility_type TO importer;
GRANT DELETE ON TABLE asset.ref_municipality TO importer;
GRANT DELETE ON TABLE asset.ref_region TO importer;
GRANT DELETE ON TABLE asset.ref_suburb TO importer;
GRANT DELETE ON TABLE asset.ref_town TO importer;
GRANT DELETE ON TABLE asset.ref_ward TO importer;

-- *********************
//

--
-- audit
--
GRANT USAGE ON SCHEMA audit TO importer;
//
-- *** Note not update ***
GRANT SELECT,INSERT ON TABLE audit.audit TO importer;
GRANT SELECT,INSERT ON TABLE audit.audit_type TO importer;
GRANT SELECT,INSERT ON TABLE audit.auditlink TO importer;
//
--
-- dtpw
--
GRANT USAGE ON SCHEMA dtpw TO importer;
//
GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_branch TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_chief_directorate TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE dtpw.ref_client_department TO importer;
//
GRANT SELECT ON  dtpw.dtpw_core_report_view TO importer;
GRANT SELECT ON  dtpw.asset_core_dtpw_view  TO importer;
GRANT SELECT ON  dtpw.asset_core_dtpw_view_with_lpi  TO importer;
//

-- *********************
-- **** DANGER ZONE ****
-- *********************
GRANT DELETE ON TABLE dtpw.ref_branch TO importer;
GRANT DELETE ON TABLE dtpw.ref_chief_directorate TO importer;
GRANT DELETE ON TABLE dtpw.ref_client_department TO importer;
-- *********************
//

--
-- access control
--
GRANT USAGE ON SCHEMA access_control TO importer;
//
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO importer;
//
GRANT EXECUTE ON FUNCTION access_control.sp_add_group TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_add_user_to_group TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_grant_access TO importer;
//
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_remove_user_from_group TO importer;
-- NO! GRANT EXECUTE ON FUNCTION access_control.sp_revoke_access TO importer;