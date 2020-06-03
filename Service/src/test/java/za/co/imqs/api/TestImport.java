package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.model.Importer;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    @Test
    public void testIt() throws Exception  {
        final Importer importer = new Importer("http://localhost:8669", session, new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://localhost:5432/test_core","postgres","1mq5p@55w0rd"
                )
        ));
        //final Path assets = Paths.get("/home/frank/Development/Core/DTPW Data/DTPW_Location Breakdown_V13B_20200212 (copy).csv");
        final Path assets = Paths.get("/home/frank/Downloads/data-1590583734568.csv");
        //final Path parcels = Paths.get("/home/frank/Development/Core/DTPW Data/LPI Land Parcels linked to AssetID_V2_20200317 (copy).csv");



        importer.importLookups("BRANCH", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_branch_202005041507.csv"));
        importer.importLookups("DISTRICT", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_district_202005041508.csv"));
        importer.importLookups("MUNIC", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_municipality_202005041508.csv"));
        importer.importLookups("REGION", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_region_202005041509.csv"));
        importer.importLookups("SUBURB", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_suburb_202005041509.csv"));
        importer.importLookups("TOWN", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_town_202005041509.csv"));
        importer.importLookups("FACIL_TYPE", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type_202005041508.csv"));


        importer.importLookups("CHIEF_DIR", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_chief_directorate_202005041507.csv" ), new LookupProvider.ChiefDirectorateKv());
        importer.importLookups("CLIENT_DEP", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_client_department_202005041508.csv"), new Importer.ClientDeptKv());

        importer.importAssets(assets);

        //importer.importLandParcel(parcels);

        // generat ea report here
    }
}
