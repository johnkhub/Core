package za.co.imqs.coreservice.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dto.asset.*;
import za.co.imqs.coreservice.dto.lookup.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Importer { // TODO split this into utility classes and DTPW specific implementation
    private static final String EMIS = "4a6a4f78-2dc4-4b29-aa9e-5033b834a564";

    enum Flags {
        FORCE_INSERT,
        FORCE_CONTINUE,
        FORCE_UPSERT
    }


    //
    //  We make path and code the same value. Both dot delimited.
    //

    private static final Predicate<org.springframework.http.HttpStatus> SUCCESS = org.springframework.http.HttpStatus::is2xxSuccessful;
    private static final Predicate<org.springframework.http.HttpStatus> FAILURE = SUCCESS.negate();

    private final String session;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final EnumSet<Flags> flags;

    public Importer(String baseUrl, String session, EnumSet<Flags> flags) {
        this.session = session;

        //this.restTemplate = new RestTemplate();  Else PATCH is not supported
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(requestFactory);
        this.baseUrl = baseUrl;
        this.flags = flags;
    }

    //
    // Utility methods to construct solution from
    //
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

    public  <T extends CoreAssetDto> void importType(Path path, T asset, BeanVerifier<T> skipper, String type, Writer exceptionFile) throws Exception {
        importType(path, asset, skipper, type, exceptionFile, (a) -> {});
    }

    public  <T extends CoreAssetDto> void importType(Path path, T asset, BeanVerifier<T> skipper, String type, Writer exceptionFile, After then) throws Exception {
        final CsvImporter<CoreAssetDto> assetImporter = new CsvImporter<>();
        final StatefulBeanToCsv<T> sbc = exceptionFile == null ? null : new StatefulBeanToCsvBuilder(exceptionFile).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();


        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, asset, skipper, type).forEach(
                    (dto) -> {
                        try {
                            if (flags.contains(Flags.FORCE_INSERT)) {
                                restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PUT, jsonEntity(dto), Void.class, dto.getAsset_id());
                            } else if (flags.contains(Flags.FORCE_UPSERT)) {
                                if (getAsset(dto.getFunc_loc_path()) == null) {
                                    restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PUT, jsonEntity(dto), Void.class, UUID.randomUUID());
                                } else {
                                    restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PATCH, jsonEntity(dto), Void.class, dto.getAsset_id());
                                }
                            } else {
                                if (dto.getAsset_id() == null) {
                                    restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PUT, jsonEntity(dto), Void.class, UUID.randomUUID());
                                } else {
                                    restTemplate.exchange(baseUrl + "/assets/{uuid}", HttpMethod.PATCH, jsonEntity(dto), Void.class, dto.getAsset_id());
                                }
                            }

                            then.perform(dto);

                        } catch (HttpClientErrorException c) {
                            dto.setError(c.getResponseBodyAsString());
                            processException(sbc, dto);

                        } catch (Exception e) {
                            dto.setError(e.getMessage());
                            processException(sbc, dto);
                        }
                    }
            );
        }
        if (exceptionFile != null) {
            exceptionFile.close();
        }
    }

    private <T> void processException(StatefulBeanToCsv<T> sbc, CoreAssetDto dto) {
        log.error(dto.getError());

        if (!flags.contains(Flags.FORCE_CONTINUE)) {
            throw new RuntimeException(dto.getError());
        }

        if (sbc != null) {
            try {
                sbc.write((T) dto);

            } catch (Exception w) {
                log.error("Unable to update exceptions file:", w);
            }
        }
    }

    public interface After<T> {
        void perform(T t);
    }

    public Map<String,String> getReverseLookups(String lookupType) throws Exception {
        final Map<String,String> valueToKey = new HashMap<>();
        final LookupProvider.Kv[] results = restTemplate.getForEntity(baseUrl+"/lookups/kv/{lookupType}", LookupProvider.Kv[].class, lookupType).getBody();
        for (LookupProvider.Kv kv : results) {
            valueToKey.put(kv.getV(), kv.getK());
        }

        return valueToKey;
    }

    public <T extends CoreAssetDto> T getAsset(String func_loc_path) throws Exception {
        final CoreAssetDto asset = restTemplate.exchange(
                baseUrl + "/assets/func_loc_path/{path}",
                HttpMethod.GET,
                jsonEntity(null),
                CoreAssetDto.class,
                func_loc_path.replace(".","+")
        ).getBody();

        return (T)asset;
    }

    //
    // Implementations for DTPW
    //
    public void importLandParcelMappings(Path path, Writer exceptionFile) throws Exception {
        log.info("Map Assets to Landparcels");
        final CsvImporter<AssetToLandparcel> assetImporter = new CsvImporter<>();
        final StatefulBeanToCsv<AssetToLandparcel> sbc = exceptionFile == null ? null : new StatefulBeanToCsvBuilder(exceptionFile).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();

        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, new AssetToLandparcel()).forEach(
                    (dto) -> {
                        try {
                            restTemplate.put(
                                    baseUrl+"/assets/landparcel/{landparcel_id}/asset/{asset_id}",
                                    null,
                                    dto.getLandparcel_asset_id(), dto.getAsset_id()
                            );
                        } catch (HttpClientErrorException c) {
                            dto.setError(c.getResponseBodyAsString());
                            processException(sbc, dto);

                        } catch (Exception e) {
                            dto.setError(e.getMessage());
                            processException(sbc, dto);
                        }
                    }
            );
        }

        if (exceptionFile != null) {
            exceptionFile.close();
        }
    }

    public void importEmis(Path path, Writer exceptionFile) throws Exception {
        final CsvImporter<ExternalLinks> assetImporter = new CsvImporter<>();
        final StatefulBeanToCsv<ExternalLinks> sbc = exceptionFile == null ? null : new StatefulBeanToCsvBuilder(exceptionFile).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();

        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, new ExternalLinks()).forEach(
                    (dto) -> {
                        try {
                            if (dto.getEmis() == null) {
                                return;
                            }

                            final CoreAssetDto asset = restTemplate.exchange(
                                        baseUrl + "/assets/func_loc_path/{path}",
                                        HttpMethod.GET,
                                        jsonEntity(null),
                                        CoreAssetDto.class,
                                        dto.getFunc_loc_path().replace(".","+")
                                ).getBody();

                            if (asset == null) {
                                throw new NotFoundException(String.format("No asset found with func_loc_path {} to link external data {} to.", dto.getFunc_loc_path(), dto.toString()));
                            }

                            final UUID assetId = UUID.fromString(asset.getAsset_id());

                            restTemplate.exchange(
                                    baseUrl + "/assets/link/{uuid}/to/{external_id_type}/{external_id}",
                                    HttpMethod.DELETE,
                                    jsonEntity(null),
                                    Void.class,
                                    assetId, EMIS, dto.getEmis()
                            );

                            restTemplate.exchange(
                                    baseUrl + "/assets/link/{uuid}/to/{external_id_type}/{external_id}",
                                    HttpMethod.PUT,
                                    jsonEntity(null),
                                    Void.class,
                                    assetId, EMIS, dto.getEmis()
                            );


                        } catch (HttpClientErrorException c) {
                            dto.setError(c.getResponseBodyAsString());
                            processException(sbc, dto);

                        } catch (Exception e) {
                            dto.setError(e.getMessage());
                            processException(sbc, dto);
                        }
                    }
            );
        }

        if (exceptionFile != null) {
            exceptionFile.close();
        }
    }

    public void importAssets(Path assets) throws Exception {
        log.info("Importing Envelopes...");
        importType(assets, new AssetEnvelopeDto(), (dto)-> { remap(dto); return true; }, "ENVELOPE", new FileWriter("envelope_exceptions.csv"));

        log.info("Importing Facilities...");
        importType(assets, new AssetFacilityDto(), (dto)->{ remap(dto); return true;},"FACILITY", new FileWriter("facility_exceptions.csv"));

        log.info("Importing Buildings...");
        importType(assets, new AssetBuildingDto(), (dto)->{ remap(dto); return true;}, "BUILDING", new FileWriter("building_exceptions.csv"));

        log.info("Importing Sites...");
        importType(assets, new AssetSiteDto(), (dto)->{ remap(dto); return true;}, "SITE", new FileWriter("site_exceptions.csv"));

        log.info("Importing Floors...");
        importType(assets, new AssetFloorDto(), (dto)->{ remap(dto); return true;}, "FLOOR",new FileWriter("floor_exceptions.csv"));

        log.info("Importing Rooms...");
        importType(assets, new AssetRoomDto(), (dto)->{ remap(dto); return true;}, "ROOM", new FileWriter("room_exceptions.csv"));

        log.info("Importing Components...");
        importType(assets, new AssetComponentDto(), (dto)->{ remap(dto); return true;}, "COMPONENT", new FileWriter("component_exceptions.csv"));

        log.info("Importing Landparcels...");
        importType(assets, new AssetLandparcelDto(), (dto)->{ remap(dto); return true;}, "LANDPARCEL", new FileWriter("landparcel_exceptions.csv"));

        log.info("Importing EMIS...");
        importEmis(assets, new FileWriter("emis_exceptions.csv"));
    }



    private void processException(StatefulBeanToCsv<AssetToLandparcel> sbc, AssetToLandparcel dto) {
        log.error(dto.getError());

        if (!flags.contains(Flags.FORCE_CONTINUE)) {
            throw new RuntimeException(dto.getError());
        }

        if (sbc != null) {
            try {
                sbc.write(dto);

            } catch (Exception w) {
                log.error("Unable to update exceptions file:", w);
            }
        }
    }

    private static <T extends CoreAssetDto> T remap(T dto) {
        dto.setCode(dto.getFunc_loc_path().replace(".", "-"));
        return dto;
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

    public static void main(String[] args) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        Config config;
        try (InputStream is = new FileInputStream(args[0])) {
            config = mapper.readerFor(Config.class).readValue(is);
        }

        final String session = getAuthSession(config.getAuthUrl(), config.getLoginUsername(), config.getLoginPassword()) ;

        final String cmd = args[1];
        final Path file = Paths.get(args[2]);
        log.info("Processing {}", file);


        if (cmd.equalsIgnoreCase("lookups")) {
            final String lookupType = args[3];
            Importer i = new Importer(config.getServiceUrl(), session, EnumSet.noneOf(Flags.class));
            i.importLookups(lookupType, file, get(lookupType));
            return;

        } else if (cmd.equalsIgnoreCase("assets")) {
            final String[] flagsS = (args.length == 4) ? args[3].split(",") : new String[0];
            final List<Flags> x = Arrays.asList(flagsS).stream().map((s)-> Flags.valueOf(s.trim())).collect(Collectors.toList());
            final EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
            flags.addAll(x);

            final EnumSet<Flags> mutuallyExclusive = EnumSet.of(Flags.FORCE_INSERT, Flags.FORCE_UPSERT);
            mutuallyExclusive.retainAll(flags);
            if (mutuallyExclusive.size() > 1) throw new IllegalArgumentException("Flags " + mutuallyExclusive + " are mutually exclusive.");

            final Importer i = new Importer(config.getServiceUrl(), session, flags);
            i.importAssets(file);
            return;

        } else if (cmd.equalsIgnoreCase("asset_to_landparcel")) {
            final String[] flagsS = (args.length == 4) ? args[3].split(",") : new String[0];
            final List<Flags> x = Arrays.asList(flagsS).stream().map((s)-> Flags.valueOf(s.trim())).collect(Collectors.toList());
            EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
            flags.addAll(x);

            Importer i = new Importer(config.getServiceUrl(), session, flags);
            i.importLandParcelMappings(file, new FileWriter("landparcel_mapping_exceptions.csv"));
            return;
        }

        throw new IllegalArgumentException("Unknown command:" + cmd);
    }

    // TODO Find a way to handle this in LookupProvider since it knows this relationship
    private static <T extends LookupProvider.Kv> T get(String s) {
        switch(s) {
            case "DISTRICT":
                return (T) new KvDistrict();
            case "MUNIC":
                return (T) new KvMunicipality();
            case "WARD":
                return (T) new KvWard();
            case "TOWN":
                return (T) new KvTown();
            case "SUBURB":
                return (T) new KvSuburb();
            case "FACIL_TYPE":
                return (T) new LookupProvider.Kv();
            case "BRANCH":
                return (T) new LookupProvider.Kv();
            case "CHIEF_DIR":
                return (T) new LookupProvider.ChiefDirectorateKv();
            case "CLIENT_DEP":
                return (T) new LookupProvider.ClientDeptKv();
        }
        return null;
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
    public static class AssetToLandparcel  {
        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String asset_id;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String landparcel_asset_id;

        @CsvBindByName(required = false)
        private String error;
    }
}
