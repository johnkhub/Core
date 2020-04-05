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

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE assettype TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE location TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset_link TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE external_id_type TO importer;

GRANT USAGE ON SCHEMA asset TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_envelope TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_landparcel TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_facility TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_building TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_site TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_floor TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_room TO importer;
GRANT SELECT,INSERT,UPDATE ON TABLE asset.a_tp_component TO importer;

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_ward TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_suburb TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_region TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_town TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_municipality TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_district TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE asset.ref_facility_type TO importer;

GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE unit TO importer;

GRANT USAGE ON SCHEMA dtpw TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_branch TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_client_department TO importer;
GRANT SELECT,INSERT,UPDATE,DELETE ON TABLE dtpw.ref_chief_directorate TO importer;

GRANT USAGE ON SCHEMA access_control TO importer;
GRANT SELECT ON TABLE access_control.entity_access TO importer;
GRANT SELECT ON TABLE access_control.principal TO importer;
GRANT SELECT ON access_control.access_type TO importer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_grant_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_revoke_access TO importer;
GRANT EXECUTE ON FUNCTION access_control.sp_add_group TO importer;

GRANT USAGE ON SCHEMA transactions TO importer;
GRANT ALL ON TABLE transactions.transaction TO importer;
GRANT ALL ON TABLE transactions.transaction_type TO importer;
GRANT ALL ON TABLE transactions.field TO importer;
