DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'report_reader') THEN

      CREATE ROLE report_reader LOGIN PASSWORD 'report_reader';
   END IF;
END
$do$;
//

--
-- dtpw
--
GRANT USAGE ON SCHEMA dtpw TO report_reader;

GRANT SELECT ON  dtpw.dtpw_core_report_view TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view_with_lpi  TO report_reader;

GRANT SELECT ON  dtpw.asset_core_dtpw_ei_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_gi_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_hi_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_rnm_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_iam_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_ppp_view  TO report_reader;

GRANT SELECT ON  dtpw.dtpw_core_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_ei_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_gi_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_hi_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_iam_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_rnm_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_ppp_report_view_wrapper  TO report_reader;

GRANT SELECT ON  dtpw.dtpw_export_view  TO report_reader;

--
-- access control
--
GRANT USAGE ON SCHEMA access_control TO report_reader;

GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO report_reader;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO report_reader;

-- GRANT ROLE report_reader TO <your role here>;
