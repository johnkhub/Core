package za.co.imqs.coreservice.imports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dto.ErrorProvider;
import za.co.imqs.coreservice.dto.asset.*;
import za.co.imqs.coreservice.model.dtpw.DTPW;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Importer extends ImporterTemplate{

    //
    //  We make path and code the same value. Both dot delimited.
    //
    private final Map<String, Constructor> constructors = new HashMap<>();

    public Importer(String baseUrl, String session) throws Exception {
        super(baseUrl, session);

        for (JsonSubTypes.Type t : LookupProvider.Kv.class.getAnnotation(JsonSubTypes.class).value()) {
            constructors.put(t.name().toUpperCase(), t.value().getConstructor());
        }
    }

    //
    // Implementations for DTPW
    //
    public void importLandParcelMappings(Path path, Writer exceptionFile, EnumSet<Flags> flags) throws Exception {
        log.info("Map Assets to Landparcels");

        importRunner(
                path, new AssetToLandparcel(), null, exceptionFile, flags,
                PASS_ALL,
                Before.IDENTITY,
                (d) -> {
                    final AssetToLandparcel dto = (AssetToLandparcel)d;
                    restTemplate.put(
                            baseUrl+"/assets/landparcel/{landparcel_id}/asset/{asset_id}",
                            null,
                            dto.getLandparcel_asset_id(), dto.getAsset_id()
                    );
                    return dto;
                },
               After.IDENTITY
        );
    }

    public void importEmis(Path path, Writer exceptionFile, EnumSet<ImporterTemplate.Flags> flags ) throws Exception {
        importRunner(
                path, new ExternalLinks(), null, exceptionFile, flags,
                PASS_ALL,
                Before.IDENTITY,
                (d) -> {
                    final ExternalLinks dto = (ExternalLinks)d;
                    if (dto.getEmis() == null) {
                        return dto;
                    }

                    final CoreAssetDto asset = restTemplate.exchange(
                            baseUrl + "/assets/func_loc_path/{path}",
                            HttpMethod.GET,
                            jsonEntity(null),
                            CoreAssetDto.class,
                            dto.getFunc_loc_path().replace(".","+")
                    ).getBody();

                    if (asset == null) {
                        throw new NotFoundException(String.format("No asset found with func_loc_path %s to link grouping data %s to.", dto.getFunc_loc_path(), dto.toString()));
                    }

                    final UUID assetId = UUID.fromString(asset.getAsset_id());

                    restTemplate.exchange(
                            baseUrl + "/assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}",
                            HttpMethod.DELETE,
                            jsonEntity(null),
                            Void.class,
                            assetId, DTPW.GROUPING_TYPE_EMIS, dto.getEmis()
                    );

                    restTemplate.exchange(
                            baseUrl + "/assets/group/{uuid}/to/{group_id_type}/{grouping_id}",
                            HttpMethod.PUT,
                            jsonEntity(null),
                            Void.class,
                            assetId, DTPW.GROUPING_TYPE_EMIS, dto.getEmis()
                    );
                    return null;
                },
                After.IDENTITY
        );
    }

    public void importExtent(Path path, Writer exceptionFile, EnumSet<ImporterTemplate.Flags> flags ) throws Exception {
        importRunner(
                path, new Extent(), null, exceptionFile, flags,
                PASS_ALL,
                Before.IDENTITY,
                (d) -> {
                        final Extent dto = (Extent)d;
                        if (dto.getExtent() == null) {
                            return dto;
                        }

                        if (dto.getExtent_unit() == null) {
                            throw new IllegalArgumentException("Extent_unit not set!");
                        }

                        final CoreAssetDto asset = restTemplate.exchange(
                                baseUrl + "/assets/func_loc_path/{path}",
                                HttpMethod.GET,
                                jsonEntity(null),
                                CoreAssetDto.class,
                                dto.getFunc_loc_path().replace(".","+")
                        ).getBody();

                        if (asset == null) {
                            throw new NotFoundException(String.format("No asset found with func_loc_path %s to link quantity data %s to.", dto.getFunc_loc_path(), dto.toString()));
                        }

                        final UUID assetId = UUID.fromString(asset.getAsset_id());

                        try {
                            restTemplate.exchange(
                                    baseUrl + "/assets/quantity/asset_id/{uuid}/name/{name}",
                                    HttpMethod.DELETE,
                                    jsonEntity(null),
                                    Void.class,
                                    assetId, "extent"
                            );
                        } catch (HttpClientErrorException ignored) {

                        }

                        final QuantityDto quantity = new QuantityDto();
                        quantity.setUnit_code(dto.getExtent_unit()); // TODO pull lookup table from server and use for validation
                        quantity.setAsset_id(UUID.fromString(dto.getAsset_id()));
                        quantity.setName("extent");
                        quantity.setNum_units(dto.getExtent());

                        restTemplate.exchange(
                                baseUrl + "/assets/quantity",
                                HttpMethod.PUT,
                                jsonEntity(quantity),
                                Void.class
                        );
                        return dto;
                },
                After.IDENTITY
        );
    }

    public  <T extends CoreAssetDto> void importLinkedData(Path path, Writer exceptionFile, T instance, Method get, String table, String field, EnumSet<Flags> flags) throws Exception {
        importRunner(
                path, instance, null, exceptionFile, flags,
                (dto) -> true,
                Before.IDENTITY,
                (d) -> {
                    T dto = (T)d;

                    String value;
                    try {
                        value = (String) get.invoke(dto);
                        if (value == null) {
                            return dto;
                        }
                    } catch(Exception e) {
                        throw new RuntimeException(e);
                    }

                    final CoreAssetDto asset = restTemplate.exchange(
                            baseUrl + "/assets/func_loc_path/{path}",
                            HttpMethod.GET,
                            jsonEntity(null),
                            CoreAssetDto.class,
                            dto.getFunc_loc_path().replace(".","+")
                    ).getBody();

                    if (asset == null) {
                        throw new NotFoundException(String.format("No asset found with func_loc_path %s to link data %s to.", dto.getFunc_loc_path(), dto.toString()));
                    }

                    final UUID assetId = UUID.fromString(asset.getAsset_id());

                    restTemplate.exchange(
                            baseUrl + "/assets/table/{table}/field/{field}/asset/{uuid}",
                            HttpMethod.DELETE,
                            jsonEntity(null),
                            Void.class,
                            table, field, assetId
                    );

                    restTemplate.exchange(
                            baseUrl + "/assets/table/{table}/field/{field}/asset/{uuid}/value/{value}",
                            HttpMethod.PUT,
                            jsonEntity(null),
                            Void.class,
                            table, field, assetId, value
                    );

                    return null;
                },
                After.IDENTITY

        );
    }

    public void importAssets(Path assets, EnumSet<ImporterTemplate.Flags> flags) throws Exception {
        long t0 = System.currentTimeMillis();

        log.info("Importing Envelopes...");
        importType(assets, new AssetEnvelopeDto(), "ENVELOPE", new FileWriter("envelope_exceptions.csv"), flags, (dto)-> { remap(dto); return true; });

        log.info("Importing Facilities...");
        importType(assets, new AssetFacilityDto(), "FACILITY", new FileWriter("facility_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Buildings...");
        importType(assets, new AssetBuildingDto(),  "BUILDING", new FileWriter("building_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Sites...");
        importType(assets, new AssetSiteDto(), "SITE", new FileWriter("site_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Floors...");
        importType(assets, new AssetFloorDto(), "FLOOR",new FileWriter("floor_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Rooms...");
        importType(assets, new AssetRoomDto(),  "ROOM", new FileWriter("room_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Components...");
        importType(assets, new AssetComponentDto(),  "COMPONENT", new FileWriter("component_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});

        log.info("Importing Landparcels...");
        importType(assets, new AssetLandparcelDto(), "LANDPARCEL", new FileWriter("landparcel_exceptions.csv"), flags, (dto)->{ remap(dto); return true;});
        log.info("Import took {} seconds", (System.currentTimeMillis()-t0)/1000); //278 464 rows

        log.info("Importing EMIS...");
        importEmis(assets, new FileWriter("emis_exceptions.csv"), flags);

        log.info("Importing Linked data...");
        importLinkedData(
                assets, new FileWriter("linked_data_exceptions.csv"),
                new eiDistrict(), eiDistrict.class.getMethod("getEi_district_code"),
                "dtpw+ei_district_link", "k_education_district",
                flags
        );

        log.info("Importing Extents");
        importExtent(assets, new FileWriter("extent_exceptions.csv"), flags);
    }

    public void deleteAssets(Path assets, EnumSet<ImporterTemplate.Flags> flags) throws Exception {
        deleteAssets(assets, new FileWriter("delete_exceptions.csv"), flags);
    }

    private static <T extends CoreAssetDto> T remap(T dto) {
        dto.setCode(dto.getFunc_loc_path().replace(".", "-"));
        return dto;
    }

    public static String getAuthSession(String authUrl, String username, String password)  {
        if (password == "") return "hkgk";
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
            Importer i = new Importer(config.getServiceUrl(), session);
            i.importLookups(lookupType, file, i.get(lookupType));
            return;

        } else if (cmd.equalsIgnoreCase("assets")) {
            final EnumSet<ImporterTemplate.Flags> flags = getFlags(args);
            final EnumSet<ImporterTemplate.Flags> mutuallyExclusive = EnumSet.of(ImporterTemplate.Flags.FORCE_INSERT, ImporterTemplate.Flags.FORCE_UPSERT);
            mutuallyExclusive.retainAll(flags);
            if (mutuallyExclusive.size() > 1) throw new IllegalArgumentException("Flags " + mutuallyExclusive + " are mutually exclusive.");

            final Importer i = new Importer(config.getServiceUrl(), session);
            i.importAssets(file, flags);
            return;

        } else if (cmd.equalsIgnoreCase("asset_to_landparcel")) {
            final EnumSet<ImporterTemplate.Flags> flags = getFlags(args);
            Importer i = new Importer(config.getServiceUrl(), session);
            i.importLandParcelMappings(file, new FileWriter("landparcel_mapping_exceptions.csv"), flags);
            return;
        } else if (cmd.equalsIgnoreCase("delete")) {
            Importer i = new Importer(config.getServiceUrl(), session);
            i.deleteAssets(file, EnumSet.noneOf(ImporterTemplate.Flags.class));
            return;
        }

        throw new IllegalArgumentException("Unknown command:" + cmd);
    }

    private <T extends LookupProvider.Kv> T get(String s) throws Exception {
        return (T)constructors.getOrDefault(s, constructors.get("KV")).newInstance();
    }

    private static EnumSet<Flags> getFlags(String[] args) {
        final String[] flagsS = (args.length >= 4) ? args[3].split(",") : new String[0];
        final List<ImporterTemplate.Flags> x = Arrays.stream(flagsS).
                map((s)-> StringUtils.isEmpty(s) ? null : ImporterTemplate.Flags.valueOf(s)).
                filter((s) -> s != null).
                collect(Collectors.toList());
        final EnumSet<ImporterTemplate.Flags> flags = EnumSet.noneOf(ImporterTemplate.Flags.class);
        flags.addAll(x);
        return flags;
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
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class Extent extends CoreAssetDto {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String extent;

        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String extent_unit;
    }

    @Data
    public static class AssetToLandparcel implements ErrorProvider {
        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String asset_id;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String landparcel_asset_id;

        @CsvBindByName(required = false)
        private String error;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class eiDistrict extends CoreAssetDto {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String ei_district_code;
    }
}
