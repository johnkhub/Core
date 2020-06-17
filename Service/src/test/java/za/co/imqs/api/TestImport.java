package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.imports.Importer;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    @Test
    public void loadLookups() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_district.csv", "DISTRICT"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_municipality.csv", "MUNIC"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_ward.csv", "WARD"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_town.csv", "TOWN"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_suburb.csv", "SUBURB"});

        // Doe snot exist in upgrade scenario
        //Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_branch.csv", "BRANCH"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_chief_directorate.csv", "CHIEF_DIR"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_client_department.csv", "CLIENT_DEP"});
    }

    @Test
    public void fixMappings() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "assets", "/home/frank/Development/Core/Service/update/00_schema/Outside a Town_Update Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Development/Core/Service/update/00_schema/Suburb updates.csv"});
    }

    @Test
    public void fixCodes() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/2324_SilverstreamPS_Update_NEW Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/2443_Silverstream SS_NEW Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/Knysna Health Clinic_UPDATE_NEW Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/20358A_New Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/Provincial Roads Department - Residence - Erf 2643 and 2645_Update_New T.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/Worcester Hospital Erica Residence_NEW TEmplate.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation/Conville PS_Facilty add_New Template.csv"});
    }

    @Test
    public void fixCodes2() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/2915_UPDATE_NEW Template.csv"});
        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/5726_UPDATE.CSV"});
        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/20358A_New Template_V2.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/Erf179_185_Update New Template.csv"});
    }

    @Test
    public void testIt() throws Exception  {
        loadLookups();
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        Importer.main(new String[]{config, "assets", "/home/frank/Downloads/data-1591959698280.csv"});

        //importer.importLandParcel(parcels);

        // generat ea report here


    }

}
