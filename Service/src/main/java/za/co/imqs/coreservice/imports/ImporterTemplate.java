package za.co.imqs.coreservice.imports;

import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
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
import za.co.imqs.coreservice.dto.ErrorProvider;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class ImporterTemplate {

    //
    // Import option flags
    //
    public enum Flags {
        FORCE_INSERT,           // Performs an INSERT rather than an UPDATE when an Asset UUID is found
        FORCE_CONTINUE,         // Writes to the exception file but does not terminate execution
        FORCE_UPSERT            // If set the importer will check if the asset exists and then perform an UPDATE else it will INSERT
    }

    public interface Before<T> {
        T perform(T t);
    }

    public interface Handler<T> {
        T perform(T t);
    }

    public interface After<T> {
        T perform(T t);
    }

    protected final RestTemplate restTemplate;
    protected final String baseUrl;
    protected final String session;

    public ImporterTemplate(String baseUrl, String session) {
        this.session = session;
        //this.restTemplate = new RestTemplate();  Else PATCH is not supported
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(requestFactory);
        this.baseUrl = baseUrl;
    }

    public static String getAuthSession(String authUrl, String username, String password)  {
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

    public  <T extends CoreAssetDto> void importType(
            Path path, T asset, String type, Writer exceptionFile, EnumSet<Flags> flags,
            BeanVerifier<T> verifier
    ) throws Exception {
        importType(path, asset, type, exceptionFile, flags, verifier, (dto) -> dto, (dto) -> dto);
    }

    public  <T extends CoreAssetDto> void importType(
            Path path, T asset, String type, Writer exceptionFile, EnumSet<Flags> flags,
            BeanVerifier<T> verifier,
            Before<T> before,
            After<T> after
    ) throws Exception {
        importRunner(
                path, asset, type, exceptionFile, flags,
                verifier,
                before,
                (d) -> {
                        T dto = (T)d;
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
                        return dto;
                },
                after
        );
    }


    public void deleteAssets(Path path, Writer exceptionFile, EnumSet<Flags> flags) throws Exception {
        importRunner(
                path, new CoreAssetDto(), null, exceptionFile, flags,
                (dto) -> true,
                (dto) -> dto,
                (d) -> {
                    final CoreAssetDto dto = (CoreAssetDto)d;
                    restTemplate.exchange(
                            baseUrl + "/assets/testing/{uuid}",
                            HttpMethod.DELETE,
                            jsonEntity(null),
                            Void.class,
                            dto.getAsset_id()
                    );
                    return dto;
                },
                (dto) -> dto
        );
    }







    protected final <T> void importRunner(
            Path path,
            T asset,
            String type, Writer exceptionFile,
            EnumSet<Flags> flags,
            BeanVerifier<T> verifier,
            Before first,
            Handler handle,
            After then) throws Exception {
        final CsvImporter<T> assetImporter = new CsvImporter<>();
        final StatefulBeanToCsv<ErrorProvider> sbc = exceptionFile == null ? null : new StatefulBeanToCsvBuilder(exceptionFile).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();

        if (!(asset instanceof ErrorProvider)) throw new IllegalArgumentException("DTO type must be a subclass of ErrorProvider!");
        try (Reader reader = Files.newBufferedReader(path)) {
            assetImporter.stream(reader, asset, verifier, type).forEach(
                    (T dto) -> {
                        try {
                            first.perform(dto);
                            handle.perform(dto);
                            then.perform(dto);

                        } catch (HttpClientErrorException c) {
                            ((ErrorProvider)dto).setError(c.getResponseBodyAsString());
                            processException(sbc, (ErrorProvider)dto, flags);

                        } catch (Exception e) {
                            ((ErrorProvider)dto).setError(e.getMessage());
                            processException(sbc, (ErrorProvider)dto, flags);
                        }
                    }
            );
        }
        if (exceptionFile != null) {
            exceptionFile.close();
        }
    }


    //
    // Writes the dto to the csv output stream
    //
    private  <T extends ErrorProvider> void processException(StatefulBeanToCsv<T> sbc, T dto,  EnumSet<Flags> flags) {
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

    protected <T> HttpEntity<T> jsonEntity(T object) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", session);
        return new HttpEntity<>(object, headers);
    }



    //
    // Building block utility methods
    //
    public <T extends CoreAssetDto> T getAsset(String func_loc_path) {
        try {
            final CoreAssetDto asset = restTemplate.exchange(
                    baseUrl + "/assets/func_loc_path/{path}",
                    HttpMethod.GET,
                    jsonEntity(null),
                    CoreAssetDto.class,
                    func_loc_path.replace(".","+")
            ).getBody();

            return (T)asset;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected Map<String,String> getReverseLookups(String lookupType) {
        final Map<String,String> valueToKey = new HashMap<>();
        final LookupProvider.Kv[] results = restTemplate.getForEntity(baseUrl+"/lookups/kv/{lookupType}", LookupProvider.Kv[].class, lookupType).getBody();
        for (LookupProvider.Kv kv : results) {
            valueToKey.put(kv.getV(), kv.getK());
        }

        return valueToKey;
    }
}
