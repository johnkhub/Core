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
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.model.CoreAsset;
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
            method = RequestMethod.GET, value = "/{uuid}"
    )
    public ResponseEntity get(@PathVariable String uuid) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity(asDto(assetReader.getAsset(asUUID(uuid))), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
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
            method = RequestMethod.DELETE, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity removeExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        try {
            assetWriter.deleteExternalLink(uuid,external_id_type,external_id);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
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

    private CoreAssetDto asDto(CoreAsset asset) {
        final CoreAssetDto dto = new CoreAssetDto();

        dto.setLatitude(safe(asset.getLatitude()));
        dto.setGeom(asset.getGeometry());
        dto.setFunc_loc_path(asset.getFunc_loc_path());
        dto.setAsset_type_code(asset.getAsset_type_code());
        dto.setName(asset.getName());
        dto.setAdm_path(asset.getAdm_path());
        dto.setBarcode(asset.getBarcode());
        dto.setCreation_date(safe(asset.getCreation_date()));
        dto.setDeactivated_at(safe(asset.getDeactivated_at()));
        dto.setSerial_number(asset.getSerial_number());
        dto.setCode(asset.getCode());
        dto.setAddress(asset.getAddress());
        dto.setLongitude(safe(asset.getLongitude()));
        return dto;
    }

    private static String safe(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
}
