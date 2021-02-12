package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import za.co.imqs.TestUtils;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.imports.Importer;

import java.io.*;
import java.util.zip.ZipInputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.fail;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    private void loadLookups() throws Exception {
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

        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_ei_district.csv", "EI_DISTR"});

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
    @Ignore
    public void testFull() throws Exception{
        loadLookups();

        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets","/home/frank/Downloads/data-1600767575421.csv","FORCE_INSERT"});
        Importer.main(new String[]{config, "assets","/home/frank/Downloads/data-1603379308409.csv","FORCE_INSERT"});


    }

    @Test
    public void loadSmallSet() throws Exception{
        loadLookups();

        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //
        // It is important that the dataset touch on the major features
        //      Geometry ✓
        //      Grouping (e.g. EMIS) ✓
        //      Quantities (e.g. extent) ✓
        //      Linked data (e.g. EI districts) ✓
        //      Tags ✗
        //

        final String file = TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv";
        Importer.main(new String[]{config, "assets", file,"FORCE_INSERT"});

        final String tempFile = "out.csv";
        try (FileOutputStream fout = new FileOutputStream(tempFile)) {
            String header = given().header("Cookie", login.getSession()).get("/download/exporter").andReturn().getHeader("Content-Disposition");
            String fileName = header.split("=")[1];
            final ZipInputStream result = new ZipInputStream(given().header("Cookie", login.getSession()).get(fileName).then().statusCode(200).extract().asInputStream());
            result.getNextEntry();

            // Write uncompressed csv to temporary file
            IOUtils.copy(result, fout);
            fout.flush();
            fout.close();

            // Read from temporary file and compare to reference
            try (
                    InputStream reference = new FileInputStream(file);
                    FileInputStream fin = new FileInputStream(tempFile)
            ) {
                assertContentEquals(reference, fin);
            }
        }
    }

    @Test
    public void testLandparcelLink() throws Exception {
        /*
        loadLookups();
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        //Importer.main(new String[]{config, "assets","/home/frank/Downloads/9jul2020.csv", "FORCE_INSERT"});
        Importer.main(new String[]{config, "asset_to_landparcel", TestUtils.resolveWorkingFolder()+"/src/test/resources/api/dummyLink.csv","FORCE_CONTINUE"});
         */
        // We will need a normal data set import to ensure that the asset includingthe landparcels exists and then
        // we need to include the landparcels
        fail("Recheck this test and the output - compare to reference dataset as in previous loadSmallDataset");
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
    public void testForceUpsert() throws Exception {
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        loadLookups();
        Importer.main(new String[]{config, "lookups", TestUtils.resolveWorkingFolder()+"/src/test/resources/lookups/ref_ei_district.csv", "EI_DISTR"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/import_data_force_upsert.csv","FORCE_UPSERT"});
    }

    @Test
    public void testForceContinue() throws Exception {
        loadLookups();
        final String config = TestUtils.resolveWorkingFolder()+"/src/test/resources/import_config.json";
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv","FORCE_INSERT"});
        Importer.main(new String[]{config, "assets", TestUtils.resolveWorkingFolder()+"/src/test/resources/small_dataset.csv","FORCE_INSERT,FORCE_CONTINUE"});
    }


    @Test
    public void delete() {
        fail("");
    }

    @Test
    public void inactive() {
        fail("");
    }

    private static void assertContentEquals(InputStream input1, InputStream input2) throws IOException {
        if (input1 == input2) {
            return;
        } else {
            if (!(input1 instanceof BufferedInputStream)) {
                input1 = new BufferedInputStream((InputStream)input1);
            }

            if (!(input2 instanceof BufferedInputStream)) {
                input2 = new BufferedInputStream((InputStream)input2);
            }

            int ch2;
            long pos = 0;
            for(int ch = ((InputStream)input1).read(); -1 != ch; ch = ((InputStream)input1).read()) {
                ch2 = ((InputStream)input2).read();
                pos++;
                if (ch != ch2) {
                    fail("Contents differ at position "+pos+". " + (char)ch + " vs "+(char)ch2);
                }
            }

            ch2 = ((InputStream)input2).read();
            if (ch2 != -1) {
                fail("Contents differ at position "+pos+". Stream 1 has more data than Stream 2");
            }
        }
    }
}
