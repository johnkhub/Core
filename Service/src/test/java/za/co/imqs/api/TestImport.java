package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.imports.Importer;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    @Ignore("Fixup")
    @Test
    public void loadLookups() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_district.csv", "DISTRICT"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_municipality.csv", "MUNIC"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_ward.csv", "WARD"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_town.csv", "TOWN"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_suburb.csv", "SUBURB"});


        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_branch.csv", "BRANCH"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_chief_directorate.csv", "CHIEF_DIR"});
        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_client_department.csv", "CLIENT_DEP"});
    }

    @Ignore("Fixup")
    @Test
    public void fixMappings() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "assets", "/home/frank/Development/Core/Service/update/00_schema/Outside a Town_Update Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Development/Core/Service/update/00_schema/Suburb updates.csv"});
    }

    @Ignore("Fixup")
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

    @Ignore("Fixup")
    @Test
    public void fixCodes2() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/2915_UPDATE_NEW Template.csv"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/5726_UPDATE.CSV"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/20358A_New Template_V2.csv"});

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation2/Erf179_185_Update New Template.csv"});
    }

    @Ignore("Fixup")
    @Test
    public void fixCodes3() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation3/Room and Floor ADD.CSV", "FORCE_CONTINUE"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation3/Room and Floor UPDATE FL and ASSET TYPE.CSV","FORCE_CONTINUE"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation3/Room and Floor UPDATE FL and NAME.CSV","FORCE_CONTINUE"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation3/Room and Floor UPDATE FL only.csv","FORCE_CONTINUE"});
    }

    @Ignore("Fixup")
    @Test
    public void fixCodes4() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation4/1557_1560_ADD.CSV"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation4/43703_43734_5761_UPDATE FL.CSV"});
    }

    @Ignore("Fixup")
    @Test
    public void fixCodes5() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation5/Rooms ADD.CSV"});

        /* You need to delete these first
        select fn_delete_asset('25d0f222-3257-46bc-bf2c-92cfc0228236');
        select fn_delete_asset('5317540e-8b61-4ad1-9b7a-91f9709bc61d');
        select fn_delete_asset('9f928ba6-a135-4ef6-80f0-551d67744590');
        select fn_delete_asset('eaee4ad6-4581-4e33-8b08-f64ec05faf16');
        */
        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation5/Rooms UPDATE_FL and ASSET TYPE.CSV", "FORCE_INSERT"});
        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation5/Rooms UPDATE_FL ONLY.CSV", "FORCE_CONTINUE"});
        //Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/Path fixes/RE__Further_data_validation5/Rooms UPDATE_FL ONLY_2.csv", "FORCE_CONTINUE"});

        /*
        then delete

        select fn_delete_asset('d6c9a3d3-3dd1-4edb-b91a-3c073386455b');
        select fn_delete_asset('1821b788-ae20-48e0-91f3-e5bfb23ba517');
        select fn_delete_asset('a8705ae6-fa06-458c-9f40-8488d2dc1cf2');
        select fn_delete_asset('3bb493bc-b66a-4594-a9d9-9448bf5f1649');
        select fn_delete_asset('7a204214-d7ad-4dc3-843e-65a9cd283993');
        */

    }

    @Ignore("Fixup")
    @Test
    public void fixCodes6() throws Exception {
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        loadLookups();

        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/RE__Land_parcels/LAT LONG ADD.CSV"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/RE__Land_parcels/LAT LONG UPDATE.CSV"});
        Importer.main(new String[]{config, "assets", "/home/frank/Documents/IMQS/DTPW/RE__Land_parcels/EMIS NR UPDATE.CSV"});

        //delete from asset_link where external_id = '102480363' and asset_id = '0b3e2c3a-ce7a-4c82-acea-2eb3ea31d692';
        //delete from asset_link where external_id = '108309284' and asset_id = '7c07e4d4-d42d-48ef-b8d0-e0d953a25f3f';
    }

    @Ignore
    @Test
    public void testIt() throws Exception  {
        loadLookups();
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", "/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        Importer.main(new String[]{config, "assets", "/home/frank/Downloads/data-1591959698280.csv", "FORCE_INSERT"});

       // generate a report here
    }

    @Test
    public void testFull() throws Exception{
        loadLookups();
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "assets","/home/frank/Downloads/9jul2020.csv", "FORCE_INSERT"});
    }

    @Test
    @Ignore
    public void testLandparcelLink() throws Exception{
        //loadLookups();
        final String config = "/home/frank/Development/Core/Service/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets","/home/frank/Downloads/9jul2020.csv", "FORCE_INSERT"});
        Importer.main(new String[]{config, "asset_to_landparcel","/home/frank/Development/Core/Service/src/test/resources/api/dummyLink.csv","FORCE_CONTINUE"});
    }

}
