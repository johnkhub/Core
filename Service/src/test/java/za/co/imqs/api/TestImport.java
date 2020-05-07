package za.co.imqs.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.*;
import za.co.imqs.coreservice.dto.imports.CsvImporter;
import za.co.imqs.coreservice.dto.imports.Rules;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;

@Slf4j
public class TestImport extends AbstractAssetControllerAPITest {

    @Test
    public void testIt() throws Exception  {
        JdbcTemplate jdbc = new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://localhost:5432/test_core","postgres","1mq5p@55w0rd"
                )
        );


        log.info("Importing lookups...");
        importLookups();

        log.info("Importing Envelopes...");
        importType(new AssetEnvelopeDto(),
                (d)-> {
                    AssetEnvelopeDto dto = (AssetEnvelopeDto)d;
                    dto.setCode(dto.getFunc_loc_path());
                    dto.setFunc_loc_path(dto.getFunc_loc_path().replace("-", "."));

                    if (dto.getTown_code() != null) dto.setTown_code(getKfromV(jdbc, "asset.ref_town", dto.getTown_code()));
                    if (dto.getSuburb_code() != null) dto.setSuburb_code(getKfromV(jdbc, "asset.ref_suburb", dto.getSuburb_code()));
                    if (dto.getMunicipality_code() != null) dto.setMunicipality_code(getKfromV(jdbc, "asset.ref_municipality", dto.getMunicipality_code()));
                    if (dto.getDistrict_code() != null) dto.setDistrict_code(getKfromV(jdbc, "asset.ref_district", dto.getDistrict_code()));
                    if (dto.getRegion_code() != null) dto.setRegion_code(getKfromV(jdbc, "asset.ref_region", dto.getRegion_code()));
                    if (dto.getWard_code() != null) dto.setWard_code(getKfromV(jdbc, "asset.ref_ward", dto.getWard_code()));

                    return dto.getAsset_type_code().equals("ENVELOPE");
                });

        log.info("Importing Facilities...");
        importType(new AssetFacilityDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("FACILITY");
                }
        );

        log.info("Importing Buildings...");
        importType(new AssetBuildingDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("BUILDING");
                });


        log.info("Importing Sites...");
        importType(new AssetSiteDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("SITE");
                }
        );

        log.info("Importing Floors...");
        importType(new AssetFloorDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("FLOOR");
                }
        );

        log.info("Importing Rooms...");
        importType(new AssetRoomDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("ROOM");
                }
        );

        log.info("Importing Components...");
        importType(new AssetComponentDto(),
                (dto)-> {
                    remap((CoreAssetDto)dto);
                    return ((CoreAssetDto)dto).getAsset_type_code().equals("COMPONENT");
                }
        );


        log.info("Importing EMIS...");
        importType(new ExternalLinks(),
                (dto)-> {
                    remap((ExternalLinks)dto);

                    if (((ExternalLinks) dto).getEmis() != null) {
                        final UUID assetId = getAssetId(jdbc, ((ExternalLinks) dto));
                        if (assetId != null) {
                            Response status = given().
                                    header("Cookie", session).
                                    put(
                                            "/assets/link/{uuid}/to/{external_id_type}/{external_id}",
                                            assetId, "4a6a4f78-2dc4-4b29-aa9e-5033b834a564", ((ExternalLinks) dto).getEmis()
                                    );

                            if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                                log.error("{} Unable to link EMIS -  {} ({})", status.getStatusCode(), dto.toString(), status.body().prettyPrint());
                                throw new RuntimeException("Fail");
                            }
                        } else {
                            log.error("No record found! {}", dto.toString());
                        }
                    }
                    return true;
                }
        );

        log.info("Import Land Parcels");
        importLandParcel();
    }

    private static CoreAssetDto remap(CoreAssetDto dto) {

        dto.setFunc_loc_path(dto.getAssetId() + "-" + dto.getFunc_loc_path());
        dto.setCode(dto.getFunc_loc_path());
        dto.setFunc_loc_path(dto.getFunc_loc_path().replace("-", "."));

        return dto;
    }

    private void importType(CoreAssetDto asset, BeanVerifier skipper) throws Exception {
        final CsvImporter<CoreAssetDto> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("/home/frank/Development/Core/DTPW Data/DTPW_Location Breakdown_V13B_20200212 (copy).csv"))) {
            assetImporter.stream(reader, asset, skipper).forEach(
                    (dto) -> {
                        final UUID asset_id = UUID.randomUUID();
                        //log.info("Adding {} {} {}", dto.getAsset_type_code(), dto.getCode(), dto.getFunc_loc_path());

                        Response status = given().
                                header("Cookie", session).
                                contentType(ContentType.JSON).body(dto).
                                param("isImport",true).
                                put("/assets/{uuid}", asset_id);

                        if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                            log.error("{} Boo boo -  {} ({})", status, dto.toString(), status.body().prettyPrint());
                            throw new RuntimeException("Fail");
                        }
                    }
            );
        }
    }

    private UUID getAssetId(JdbcTemplate jdbc, CoreAssetDto dto) {
        try {
            return UUID.fromString(jdbc.queryForObject("SELECT asset_id FROM asset WHERE code = ?", String.class, dto.getCode()));
        } catch (IncorrectResultSizeDataAccessException ignore) {
            return null;
        }
    }

    private static String getKfromV(JdbcTemplate jdbc, String lookup_table, String v) {
        return jdbc.queryForObject("SELECT k FROM "+lookup_table+" WHERE v = ?", String.class, v);
    }

    @Test
    @Ignore("Run as part of testIt()")
    public void importLookups() throws Exception  {
        final Map<String, Path> files = new HashMap<>();

        files.put("BRANCH", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_branch_202005041507.csv"));
        files.put("DISTRICT", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_district_202005041508.csv"));
        files.put("MUNIC", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_municipality_202005041508.csv"));
        files.put("REGION", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_region_202005041509.csv"));
        files.put("SUBURB", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_suburb_202005041509.csv"));
        files.put("TOWN", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_town_202005041509.csv"));
        files.put("FACIL_TYPE", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_facility_type_202005041508.csv"));

        files.put("CHIEF_DIR", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_chief_directorate_202005041507.csv" ));
        files.put("CLIENT_DEP", Paths.get("/home/frank/Development/Core/Service/src/test/resources/lookups/ref_client_department_202005041508.csv"));


        for (Map.Entry<String,Path> e : files.entrySet()) {
            final CsvImporter<LookupProvider.Kv> kvImporter = new CsvImporter<>();
            try (Reader reader = Files.newBufferedReader(e.getValue())) {
                final String lookupType = e.getKey();

                kvImporter.stream(reader, getClass(lookupType)).forEach(
                        (dto) -> {
                            log.debug("{} {}:{}", lookupType, dto.getK(), dto.getV());

                            dto.setCreation_date(null); // TODO must validate this in CSV import
                            dto.setActivated_at(null); // TODO must validate this in CSV import

                            given().
                                    header("Cookie", session).
                                    contentType(ContentType.JSON).body(Collections.singleton(dto)).
                                    put("/lookups/kv/{target}", lookupType).
                                    then().statusCode(200);
                        }
                );
            }
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

    @Data
    public static class LandParcel {
        @CsvBindByName(required = true)  private String assetId;
        @CsvBindByName(required = false) private String description;
        @CsvBindByName(required = true) private String lpi;
    }

    @Data
    public static class ClientDeptKv extends LookupProvider.Kv {
        @CsvBindByName(required = false) private String chief_directorate_code;
        @CsvBindByName(required = false) private String responsible_dept_classif;

    }

    @Data
    public static class ChiefDirectorateKv extends LookupProvider.Kv {
        @CsvBindByName(required = false) private String branch_code;
    }

    private static LookupProvider.Kv getClass(String name) {
        if (name.equals("CHIEF_DIR"))
            return new ChiefDirectorateKv();
        else  if (name.equals("CLIENT_DEP"))
            return new ClientDeptKv();
        else
            return new LookupProvider.Kv();
    }

    private void importLandParcel() throws Exception {
        final CsvImporter<LandParcel> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("/home/frank/Development/Core/DTPW Data/LPI Land Parcels linked to AssetID_V2_20200317 (copy).csv"))) {
            assetImporter.stream(reader, new LandParcel()).forEach(
                    (dto) -> {
                        final AssetLandparcelDto parcelDto = new AssetLandparcelDto();
                        parcelDto.setAssetId(UUID.randomUUID().toString());
                        parcelDto.setName("Parcel " + dto.getLpi() );
                        parcelDto.setFunc_loc_path(dto.getAssetId()+"."+dto.getLpi());
                        parcelDto.setCode(dto.getLpi());
                        parcelDto.setAsset_type_code("LANDPARCEL");
                        parcelDto.setLpi(dto.getLpi());
                        parcelDto.setDescription(dto.getDescription());

                        Response status = given().
                                header("Cookie", session).
                                contentType(ContentType.JSON).body(parcelDto).
                                param("isImport",true).
                                put("/assets/{uuid}", parcelDto.getAssetId());

                        if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                            log.error("{} Boo boo -  {} ({})", status.statusCode(), parcelDto.toString(), status.body().prettyPrint());
                            throw new RuntimeException("Fail");
                        }
                    }
            );
        }
    }
}
