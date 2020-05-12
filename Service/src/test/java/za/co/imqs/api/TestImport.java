package za.co.imqs.api;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.dto.imports.Rules;
import za.co.imqs.coreservice.model.Importer;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    @Test
    public void testIt() throws Exception  {
        Importer importer = new Importer(session);
        Path assets = Paths.get("/home/frank/Development/Core/DTPW Data/DTPW_Location Breakdown_V13B_20200212 (copy).csv");

        JdbcTemplate jdbc = new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://localhost:5432/test_core","postgres","1mq5p@55w0rd"
                )
        );
/*
        importer.importLookups("BRANCH", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_branch_202005041507.csv"));
        importer.importLookups("DISTRICT", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_district_202005041508.csv"));
        importer.importLookups("MUNIC", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_municipality_202005041508.csv"));
        importer.importLookups("REGION", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_region_202005041509.csv"));
        importer.importLookups("SUBURB", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_suburb_202005041509.csv"));
        importer.importLookups("TOWN", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_town_202005041509.csv"));
        importer.importLookups("FACIL_TYPE", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type_202005041508.csv"));

  */
        importer.importLookups("CHIEF_DIR", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_chief_directorate_202005041507.csv" ), new LookupProvider.ChiefDirectorateKv());
        //importer.importLookups("CLIENT_DEPT", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_client_department_202005041508.csv"), new ClientDeptKv());

/*
        final Map<String,String> towns = importer.getReverseLookups("TOWN");
        final Map<String,String> suburbs = importer.getReverseLookups("SUBURB");
        final Map<String,String> municipality = importer.getReverseLookups("MUNIC");
        final Map<String,String> district = importer.getReverseLookups("DISTRICT");

        log.info("Importing Envelopes...");
        importer.importType(assets, new AssetEnvelopeDto(),
                (dto)-> {
                    if (dto.getAsset_type_code().equals("ENVELOPE")) {
                        dto.setCode(dto.getFunc_loc_path());
                        dto.setFunc_loc_path(dto.getFunc_loc_path().replace("-", "."));

                        if (dto.getTown_code() != null) dto.setTown_code(towns.get(dto.getTown_code()));
                        if (dto.getSuburb_code() != null) dto.setSuburb_code(suburbs.get(dto.getSuburb_code()));
                        if (dto.getMunicipality_code() != null)
                            dto.setMunicipality_code(municipality.get(dto.getMunicipality_code()));
                        if (dto.getDistrict_code() != null) dto.setDistrict_code(district.get(dto.getDistrict_code()));

                        return true;
                    }
                    return false;
                });

        log.info("Importing Facilities...");
        importer.importType(assets, new AssetFacilityDto(), (dto)->remap(dto).getAsset_type_code().equals("FACILITY"));

        log.info("Importing Buildings...");
        importer.importType(assets, new AssetBuildingDto(), (dto) ->  remap(dto).getAsset_type_code().equals("BUILDING"));

        log.info("Importing Sites...");
        importer.importType(assets, new AssetSiteDto(), (dto)-> remap(dto).getAsset_type_code().equals("SITE"));

        log.info("Importing Floors...");
        importer.importType(assets, new AssetFloorDto(), (dto)-> remap(dto).getAsset_type_code().equals("FLOOR"));

        log.info("Importing Rooms...");
        importer.importType(assets, new AssetRoomDto(), (dto)-> remap(dto).getAsset_type_code().equals("ROOM"));

        log.info("Importing Components...");
        importer.importType(assets, new AssetComponentDto(), (dto)-> remap(dto).getAsset_type_code().equals("COMPONENT"));


        log.info("Importing EMIS...");
        importer.importType(assets, new ExternalLinks(),
                (dto)-> {
                    final ExternalLinks e = remap(dto);

                    if (e.getEmis() != null) {
                        final UUID assetId = getAssetId(jdbc, e);
                        if (assetId != null) {
                            Response status = given().
                                    header("Cookie", session).
                                    put(
                                            "/assets/link/{uuid}/to/{external_id_type}/{external_id}",
                                            assetId, "4a6a4f78-2dc4-4b29-aa9e-5033b834a564", e.getEmis()
                                    );

                            if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                                throw new RuntimeException(String.format("Unable to link EMIS  %s. %s (%s)", dto.toString(), status.getStatusCode(),status.body().prettyPrint()));
                            }
                        } else {
                            log.warn("No asset found with code {} to link external data {} to.", dto.getCode(), dto.toString());
                        }
                    }
                    return true;
                }
        );

        log.info("Import Land Parcels");
        importer.importLandParcel(Paths.get("/home/frank/Development/Core/DTPW Data/LPI Land Parcels linked to AssetID_V2_20200317 (copy).csv"));
        */

    }

    private static <T extends CoreAssetDto> T remap(T dto) {
        dto.setFunc_loc_path(dto.getAssetId() + "-" + dto.getFunc_loc_path());
        dto.setCode(dto.getFunc_loc_path());
        dto.setFunc_loc_path(dto.getFunc_loc_path().replace("-", "."));

        return dto;
    }


    private UUID getAssetId(JdbcTemplate jdbc, CoreAssetDto dto) {
        try {
            return UUID.fromString(jdbc.queryForObject("SELECT asset_id FROM asset WHERE code = ?", String.class, dto.getCode()));
        } catch (IncorrectResultSizeDataAccessException ignore) {
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class ExternalLinks extends CoreAssetDto {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String emis;
    }
}
