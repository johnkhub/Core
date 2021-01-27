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


GRANT SELECT ON TABLE dtpw.ref_accommodation_type TO normal_reader;
GRANT SELECT ON TABLE public.ref_district TO normal_reader;
GRANT SELECT ON TABLE public.ref_town TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_land_use_class TO normal_reader;
GRANT SELECT ON TABLE public.ref_accessibility_rating TO normal_reader;
GRANT SELECT ON TABLE public.ref_asset_class TO normal_reader;
GRANT SELECT ON TABLE public.ref_region TO normal_reader;
GRANT SELECT ON TABLE public.ref_asset_nature TO normal_reader;
GRANT SELECT ON TABLE public.ref_ward TO normal_reader;
GRANT SELECT ON TABLE public.ref_condition_rating TO normal_reader;
GRANT SELECT ON TABLE public.ref_confidence_rating TO normal_reader;
GRANT SELECT ON TABLE public.ref_criticality_rating TO normal_reader;
GRANT SELECT ON TABLE public.ref_performance_rating TO normal_reader;
GRANT SELECT ON TABLE public.ref_utilisation_rating TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_deed_office TO normal_reader;
GRANT SELECT ON TABLE asset.ref_facility_type TO normal_reader;
GRANT SELECT ON TABLE public.ref_municipality TO normal_reader;
GRANT SELECT ON TABLE public.ref_suburb TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_branch TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_chief_directorate TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_client_department TO normal_reader;
GRANT SELECT ON TABLE dtpw.ref_ei_district TO normal_reader;

-- GRANT ROLE report_reader TO <your role here>;
