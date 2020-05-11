package za.co.imqs.coreservice.model;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.httpclient.HttpStatus;
import za.co.imqs.coreservice.CliHandler;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.AssetLandparcelDto;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.dto.imports.CsvImporter;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;

@Slf4j
public class Importer implements CliHandler {
    private String session;

    public Importer(String session) {
        this.session = session;
    }

    public void importLookups(String lookupType, Path path) throws Exception  {
        log.info("Importing Lookup {} from {}", lookupType, path.toString());
        final CsvImporter<LookupProvider.Kv> kvImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            kvImporter.stream(reader, new LookupProvider.Kv()).forEach(
                    (dto) -> {
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

    public  <T extends CoreAssetDto> void importType(Path path, T asset, BeanVerifier<T> skipper) throws Exception {
        final CsvImporter<CoreAssetDto> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, asset, skipper).forEach(
                    (dto) -> {
                        final UUID asset_id = UUID.randomUUID();

                        Response status = given().
                                header("Cookie", session).
                                contentType(ContentType.JSON).body(dto).
                                param("isImport",true).
                                put("/assets/{uuid}", asset_id);

                        if (status.getStatusCode() != HttpStatus.SC_CREATED) {
                            throw new RuntimeException(String.format("Could not import Asset %s:  %s (%s)",  dto.toString(), status, status.body().prettyPrint()));
                        }
                    }
            );
        }
    }

    public Map<String,String> getReverseLookups(String lookupType) throws Exception {
        final Map<String,String> valueToKey = new HashMap<>();
        LookupProvider.Kv[] results = given().
                header("Cookie", session).
                get("/lookups/kv/{lookupType}", lookupType).then().extract().body().as(LookupProvider.Kv[].class);

        for (LookupProvider.Kv kv : results) {
            valueToKey.put(kv.getV(), kv.getK());
        }

        return valueToKey;
    }

    public void importLandParcel(Path path) throws Exception {
        final CsvImporter<ImportedLandParcel> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, new ImportedLandParcel()).forEach(
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
                            throw new RuntimeException(String.format("Could not import Asset %s:  %s (%s)",  dto.toString(), status, status.body().prettyPrint()));
                        }
                    }
            );
        }
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public OptionGroup getOptionGroup() {
        return null;
    }

    @Override
    public boolean handle(CommandLine cmd, Options options) {
        return false;
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

    @Data
    public static class ImportedLandParcel {
        @CsvBindByName(required = true)  private String assetId;
        @CsvBindByName(required = false) private String description;
        @CsvBindByName(required = true) private String lpi;
    }
}
