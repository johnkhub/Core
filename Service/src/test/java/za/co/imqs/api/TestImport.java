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
    public void pwei164() throws Exception{
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 18/PWEI-164/Tygersig PS and Uitzig SS update.csv"});
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

    public void addLandparcels() throws Exception {
        // run storedprocs sql
        // set admin mode
        // execute below

        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //ok
        Importer.main(new String[]{config, "delete","/home/frank/Downloads/Updates/Release 2021.1 (Data only)/Landparcel Update V2/2021 Envelope_Facility_Building_Floor_Room_Site ARCHIVE or DELETE.csv", "HARD_DELETE"});
        //ok
        Importer.main(new String[]{config, "delete","/home/frank/Downloads/Updates/Release 2021.1 (Data only)/Landparcel Update V2/2021 LAndparcels ARCHIVE or DELETE_LPI Add.csv", "HARD_DELETE"});

        // should probably run these in sequence once all pass
        //to burgert some lpi already in use Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 2021.1 (Data only)/Landparcel Update V2/2021 LAndparcels ADDITIONS_LPI Add_V4.csv", "FORCE_UPSERT"});
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 2021.1 (Data only)/Landparcel Update V2/2021 LAndparcels_New Envelopes and Facilities_ADDITIONS_Exceptions Removed_V4.csv"});
        //ok Importer.main(new String[]{config, "assets","/home/frank/Downloads/Updates/Release 2021.1 (Data only)/Landparcel Update V2/2021 LAndparcels UPDATE_LPI Add_V6.csv"});
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
    public void testForceUpsert() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        loadLookups();
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_ei_district.csv", "EI_DISTR"});

        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/import_data_force_upsert.csv","FORCE_UPSERT"});
    }

    @Test
    public void testForceContinue() throws Exception {
        loadLookups();
        loadEIlookups();
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
       // Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv","FORCE_INSERT"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv","FORCE_INSERT,FORCE_CONTINUE"});
    }
}
