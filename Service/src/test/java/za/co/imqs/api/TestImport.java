package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import za.co.imqs.TestUtils;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.imports.Importer;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {
        
    @Ignore("Fixup")
    @Test
    public void loadLookups() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_district.csv", "DISTRICT"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_municipality.csv", "MUNIC"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_ward.csv", "WARD"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_town.csv", "TOWN"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_suburb.csv", "SUBURB"});

        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_branch.csv", "BRANCH"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_chief_directorate.csv", "CHIEF_DIR"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_client_department.csv", "CLIENT_DEP"});
    }

    @Test
    public void loadEIlookups() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_ei_district.csv", "EI_DISTR"});
    }

    @Test
    @Ignore
    public void testFull() throws Exception{
        loadLookups();
        loadEIlookups();

        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets","/home/frank/Downloads/data-1600767575421.csv","FORCE_INSERT"});
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/data-1603379308409.csv","FORCE_INSERT"});


    }

    @Test
    @Ignore
    public void pwei136() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        // select fn_delete_asset('43ff9429-58d5-467a-a06f-9f6f2a113c4e');
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/PWEI-136/TO be deleted - Extra Envelope for Durbanville HS.csv"});
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/PWEI-136/Update of the FUNC LOc PAth for Hostel Facility at Durbanville HS.csv"});
    }

    @Test
    @Ignore
    public void pwei145() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/PWEI-145/Update of Geometries for Switched Lat Longs.csv"});
    }

    @Test
    @Ignore
    public void pwei140() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "delete","/home/frank/Downloads/Updates/PWEI-140/TO be deleted - Extra Envelope and FAcility for Blue Downs PS.csv"});
        //select fn_delete_asset('fd2b6d63-494c-4243-8e44-8864650b7479');
        //select fn_delete_asset('cb4d7eaf-fe25-47b7-8025-d3f6f7ef62f2');

        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/PWEI-140/Update of the FUNC LOc PAth for Blue Downs PS.csv"});
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/PWEI-140/Update of the FUNC LOc PAth for Blue Downs PS (8e52522d-f008-45e3-88db-e9a1ffdd49b0).csv"});
    }

    @Test
    public void pwei160() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 18/PWEI-160/UPDATEs to EI Disticts_Envelopes1.csv"});
    }

    @Test
    public void loadSmallSet() throws Exception{
        loadLookups();
        loadEIlookups();
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv","FORCE_INSERT"});

        // TODO do an export and binary compare of the data
    }

    @Test
    @Ignore
    public void testLandparcelLink() throws Exception {
        loadLookups();
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets","/home/frank/Downloads/9jul2020.csv", "FORCE_INSERT"});
        Importer.main(new String[]{config, "asset_to_landparcel", TestUtils.resolveWorkingFolder()+"/src/test/resources/api/dummyLink.csv","FORCE_CONTINUE"});
    }

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testFlags() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv","FORCE_INSERT"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv","FORCE_UPSERT"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv","FORCE_CONTINUE"});

        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv", "FORCE_INSERT", "FORCE_CONTINUE"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv", "FORCE_UPSERT", "FORCE_CONTINUE"});

        expected.expect(IllegalArgumentException.class);
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/empty.csv", "FORCE_UPSERT,FORCE_INSERT"});
    }


    @Test
    public void addNewLookups() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";

        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_accessibility_rating.csv","ACCESSIBILITY_RATING"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_accomodation_type.csv","ACCOMODATION_TYPE"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_asset_class.csv","ASSET_CLASS"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_asset_nature.csv","ASSET_NATURE"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_condition_rating.csv","CONDITION_RATING"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_confidence_rating.csv","CONFIDENCE_RATING"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_criticality_rating.csv","CRITICALITY_RATING"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_deed_office.csv","DEED_OFFICE"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_land_use_class.csv","LAND_USE_CLASS"});
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_utilisation_rating.csv","UTILISATION_RATING"});
    }
}
