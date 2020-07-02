package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.CoreAssetReader;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.UUID;

import static za.co.imqs.coreservice.Validation.asUUID;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_TESTING_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 */
@SuppressWarnings("rawtypes")
@Profile(PROFILE_TEST)
@RestController
@Slf4j
@RequestMapping(ASSET_TESTING_PATH)
public class AssetTestingController {

    private final CoreAssetWriter assetWriter;
    private final CoreAssetReader assetReader;

    @Autowired
    public AssetTestingController(
            CoreAssetWriter assetWriter,
            CoreAssetReader assetReader
    ) {
        this.assetWriter = assetWriter;
        this.assetReader = assetReader;
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{uuid}"
    )
    public ResponseEntity delete(@PathVariable String uuid) {
        final UserContext user = ThreadLocalUser.get();
        assetWriter.obliterateAssets(asUUID(uuid));
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/link/{uuid}/to/{external_id_type}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity<>(assetReader.getExternalLinks(uuid,external_id_type), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }
}
