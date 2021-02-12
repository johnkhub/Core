package za.co.imqs.coreservice.reports;


public class DtpwReport {
    public static final String CORE_QUERY = "SELECT * FROM dtpw.dtpw_export_view ORDER BY func_loc_path";

    public static final String DUPLICATE_ASSET_QUERY = "SELECT * FROM dtpw.duplicated_emis_dtpw_view ORDER BY \"EMIS\",asset_id";


}
