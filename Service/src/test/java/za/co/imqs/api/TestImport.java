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
public class TestImport /*extends AbstractAssetControllerAPITest*/ {
        
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
    public void loadExtents() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 19/data-1607496098896_EXTENT_Extent Unit ADDv3.csv"});
    }

    @Test
    public void pwei164() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 19/PWEI-164/Tygersig PS and Uitzig SS update.csv"});
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

    @Test
    public void random_19() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets", "/home/frank/Downloads/Updates/Release 19/Random_data/Update_From_Suspect_List.csv"});
        Importer.main(new String[]{config, "delete", "/home/frank/Downloads/Updates/Release 19/Random_data/Delete_From_Suspect_List.csv", "HARD_DELETE"});
    }
}
