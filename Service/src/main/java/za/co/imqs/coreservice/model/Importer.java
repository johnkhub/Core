package za.co.imqs.coreservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.*;
import za.co.imqs.coreservice.dto.imports.CsvImporter;
import za.co.imqs.coreservice.dto.imports.Rules;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import javax.sql.DataSource;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class Importer {
    private static final boolean FORCE_INSERT = false;
    //
    //  We make path and code the same value. Both dot delimited.
    //

    private static final Predicate<org.springframework.http.HttpStatus> SUCCESS = org.springframework.http.HttpStatus::is2xxSuccessful;
    private static final Predicate<org.springframework.http.HttpStatus> FAILURE = SUCCESS.negate();

    private String session;
    private JdbcTemplate jdbc;
    private RestTemplate restTemplate;
    private String baseUrl;

    public Importer(String baseUrl, String session, JdbcTemplate jdbc) {
        this.session = session;
        this.jdbc = jdbc;

        //this.restTemplate = new RestTemplate();  Else PATCH is not supported
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(requestFactory);
        this.baseUrl = baseUrl;
    }

    public void importLookups(String lookupType, Path path) throws Exception  {
        importLookups(lookupType, path, new LookupProvider.Kv());
    }

    public <T extends LookupProvider.Kv> void importLookups(String lookupType, Path path, T kv) throws Exception  {
        log.info("Importing Lookup {} from {}", lookupType, path.toString());
        final CsvImporter<T> kvImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            kvImporter.stream(reader, kv).forEach(
                    (dto) -> {
                        if (kv.getClass() != LookupProvider.Kv.class) {
                            dto.setType(lookupType);
                        }

                        dto.setCreation_date(null); // TODO must validate this in CSV import
                        dto.setActivated_at(null); // TODO must validate this in CSV import

                        restTemplate.exchange(baseUrl+"/lookups/kv/{target}", HttpMethod.PUT, jsonEntity(Collections.singleton(dto)), Void.class, lookupType);
                    }
            );
        }
    }

    public  <T extends CoreAssetDto> void importType(Path path, T asset, BeanVerifier<T> skipper, String type) throws Exception {
        final CsvImporter<CoreAssetDto> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, asset, skipper, type).forEach(
                    (dto) -> {
                        if (FORCE_INSERT) {
                            restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PUT, jsonEntity(dto), Void.class, dto.getAsset_id());
                        } else {
                            if (dto.getAsset_id() == null) {
                                restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PUT, jsonEntity(dto), Void.class, UUID.randomUUID());
                            } else {
                                restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PATCH, jsonEntity(dto), Void.class, dto.getAsset_id());
                            }
                        }
                    }
            );
        }
    }

    public Map<String,String> getReverseLookups(String lookupType) throws Exception {
        final Map<String,String> valueToKey = new HashMap<>();
        final LookupProvider.Kv[] results = restTemplate.getForEntity(baseUrl+"/lookups/kv/{lookupType}", LookupProvider.Kv[].class, lookupType).getBody();
        for (LookupProvider.Kv kv : results) {
            valueToKey.put(kv.getV(), kv.getK());
        }

        return valueToKey;
    }

    public void importLandParcel(Path path) throws Exception {
        log.info("Import Land Parcels");
        final CsvImporter<ImportedLandParcel> assetImporter = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, new ImportedLandParcel()).forEach(
                    (dto) -> {
                        final AssetLandparcelDto parcelDto = new AssetLandparcelDto();
                        parcelDto.setAsset_id(UUID.randomUUID().toString());
                        
                        parcelDto.setName("Parcel " + dto.getLpi() );
                        parcelDto.setFunc_loc_path(dto.getAssetId()+"."+dto.getLpi());
                        parcelDto.setCode(dto.getLpi());

                        parcelDto.setAsset_type_code("LANDPARCEL");
                        parcelDto.setLpi(dto.getLpi());
                        parcelDto.setDescription(dto.getDescription());

                        restTemplate.exchange(baseUrl+"/assets/{uuid}", HttpMethod.PUT, jsonEntity(parcelDto), Void.class, parcelDto.getAsset_id());
                    }
            );
        }
    }

    @Data
    public static class ClientDeptKv extends LookupProvider.Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String chief_directorate_code;

        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String responsible_dept_classif;

    }

    @Data
    public static class ChiefDirectorateKv extends LookupProvider.Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String branch_code;
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
    public static class ImportedLandParcel {
        @CsvBindByName(required = true)
        private String assetId;

        @CsvBindByName(required = true)
        private String client_asset_id;

        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String description;

        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String lpi;
    }

    public void importAssets(Path assets) throws Exception {
        final Map<String,String> towns = getReverseLookups("TOWN");
        final Map<String,String> suburbs = getReverseLookups("SUBURB");
        final Map<String,String> municipality = getReverseLookups("MUNIC");
        final Map<String,String> district = getReverseLookups("DISTRICT");

        log.info("Importing Envelopes...");
        importType(assets, new AssetEnvelopeDto(),
                (dto)-> {
                   dto = remap(dto);

                    if (dto.getTown_code() != null) dto.setTown_code(towns.get(dto.getTown_code()));
                    //if (dto.getSuburb_code() != null) dto.setSuburb_code(suburbs.get(dto.getSuburb_code())); not reverse lookup
                    if (dto.getMunicipality_code() != null)
                        dto.setMunicipality_code(municipality.get(dto.getMunicipality_code()));
                    if (dto.getDistrict_code() != null) dto.setDistrict_code(district.get(dto.getDistrict_code()));

                    return true;
                }, "ENVELOPE");


        log.info("Importing Facilities...");
        importType(assets, new AssetFacilityDto(), (dto)->{ remap(dto); return true;},"FACILITY");

        log.info("Importing Buildings...");
        importType(assets, new AssetBuildingDto(), (dto)->{ remap(dto); return true;}, "BUILDING");

        log.info("Importing Sites...");
        importType(assets, new AssetSiteDto(), (dto)->{ remap(dto); return true;}, "SITE");

        log.info("Importing Floors...");
        importType(assets, new AssetFloorDto(), (dto)->{ remap(dto); return true;}, "FLOOR");

        log.info("Importing Rooms...");
        importType(assets, new AssetRoomDto(), (dto)->{ remap(dto); return true;}, "ROOM");

        log.info("Importing Components...");
        importType(assets, new AssetComponentDto(), (dto)->{ remap(dto); return true;}, "COMPONENT");

        log.info("Importing EMIS...");
        Map<String,UUID> codeToUuid = cacheCodeToUuid(jdbc);
        importType(assets, new ExternalLinks(),
                (dto)-> {
                    if (dto.getEmis() != null) {
                        final UUID assetId = codeToUuid.get(dto.getFunc_loc_path());
                        if (assetId != null) {
                            // TODO remove previous mapping here
                            restTemplate.exchange(
                                    baseUrl+"/assets/link/{uuid}/to/{external_id_type}/{external_id}",
                                    HttpMethod.PUT,
                                    jsonEntity(null),
                                    Void.class,
                                    assetId, "4a6a4f78-2dc4-4b29-aa9e-5033b834a564", dto.getEmis()
                            );
                        } else {
                            log.warn("No asset found with code {} to link external data {} to.", dto.getCode(), dto.toString());
                        }
                    }

                    remap(dto); // this must happen last

                    return false; // we don't want to add assets
                }, null
        );


        /*
        log.info("Importing Land Parcels...");
        importType(assets, new AssetLandparcelDto(), (dto)-> remap(dto).getAsset_type_code().equals("LANDPARCEL");
        // TODO add code to link landparcels to envelopes here

         */
    }

    private static <T extends CoreAssetDto> T remap(T dto) {
        dto.setCode(dto.getFunc_loc_path().replace(".", "-"));
        if (dto.getAsset_id() != null && !FORCE_INSERT) {
            dto.setFunc_loc_path(null);
            dto.setCode(null);
        }
        return dto;
    }


    private Map<String,UUID> cacheCodeToUuid(JdbcTemplate jdbc) {
        final Map<String,UUID> results = new HashMap<>();
        jdbc.query("SELECT asset_id, code FROM asset", (rs, i) -> {
            results.put(rs.getString("code"), UUID.fromString(rs.getString("asset_id")));
            return null;
        });
        return results;
    }

    private static String getAuthSession(String authUrl, String username, String password)  {
        try {
            HttpClient client = new HttpClient(new SimpleHttpConnectionManager());
            PostMethod post = new PostMethod(authUrl);
            post.setRequestHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            client.executeMethod(post);
            if (post.getStatusCode() != 200)
                throw new RuntimeException(String.format("Unable to log in to local IMQS instance with username %s (%s, %s)", username, post.getStatusCode(), new String(post.getResponseBody())));
            return post.getResponseHeader("Set-Cookie").getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> HttpEntity<T> jsonEntity(T object) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", session);
        return new HttpEntity<>(object, headers);
    }

    public static int main(String[] args) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final Config config = mapper.readerFor(Config.class).readValue(args[0]);

        // TODO get these from a file should be the first parameter
        final String session = getAuthSession(config.getAuthUrl(), args[1], args[2]) ;
        final DataSource ds = HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                config.getJdbcUrl(),config.getDbUsername(), config.getDbPassword()
        );
        final JdbcTemplate jdbc = new JdbcTemplate(ds);


        final String cmd = args[3];
        final Path file = Paths.get(args[4]);

        if (cmd.equalsIgnoreCase("lookups")) {
            Importer i = new Importer("", session, jdbc);
            i.importLookups(args[5], file);
        }

        if (cmd.equalsIgnoreCase("assets")) {
            Importer i = new Importer("", session, jdbc);
            i.importAssets(file);
        }

        if (cmd.equalsIgnoreCase("landparcels")) {
            Importer i = new Importer("",session, jdbc);
            i.importLandParcel(file);
        }

        return 0;
    }

    @Data
    private class Config {
        private String jdbcUrl;
        private String authUrl;
        private String dbUsername;
        private String dbPassword;
    }
}
