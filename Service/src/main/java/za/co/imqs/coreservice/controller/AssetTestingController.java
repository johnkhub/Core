package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import static za.co.imqs.coreservice.model.Validation.asUUID;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_TESTING_PATH;
import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 */
@SuppressWarnings("rawtypes")
@Profile({PROFILE_TEST, PROFILE_ADMIN})
@RestController
@Slf4j
@RequestMapping(ASSET_TESTING_PATH)
public class AssetTestingController {

    private final CoreAssetWriter assetWriter;

    @Autowired
    public AssetTestingController(
            @Qualifier("core_writer") CoreAssetWriter assetWriter
    ) {
        this.assetWriter = assetWriter;
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{uuid}"
    )
    public ResponseEntity delete(@PathVariable String uuid) {
        final UserContext user = ThreadLocalUser.get();
        assetWriter.obliterateAssets(asUUID(uuid));
        return new ResponseEntity(HttpStatus.OK);
    }
}
