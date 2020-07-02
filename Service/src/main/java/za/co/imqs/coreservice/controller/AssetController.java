package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.dataaccess.CoreAssetReader;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.model.AssetFactory;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static za.co.imqs.coreservice.Validation.asUUID;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.audit.AuditLogEntry.of;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@SuppressWarnings("rawtypes")
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH)
public class AssetController {

    private final CoreAssetWriter assetWriter;
    private final CoreAssetReader assetReader;

    private final AssetFactory aFact = new AssetFactory();
    private final AuditLoggingProxy audit;

    @Autowired
    public AssetController(
            CoreAssetWriter assetWriter,
            CoreAssetReader assetReader,
            AuditLogger auditLogger
    ) {
        this.assetWriter = assetWriter;
        this.assetReader = assetReader;
        this.audit = new AuditLoggingProxy(auditLogger);
    }

    @RequestMapping(
            method = RequestMethod.PUT, value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addAsset(
            @PathVariable UUID uuid,
            @RequestBody CoreAssetDto asset,
            @RequestParam(required = false, defaultValue ="false", name="testRun") boolean testRun
    ) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.ADD_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.createAssets(Collections.singletonList(aFact.create(uuid, asset)));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{uuid}"
    )
    public ResponseEntity deleteAsset(@PathVariable UUID uuid) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            final Map<String,UUID> p = new HashMap<>();


            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.DELETE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.deleteAssets(Collections.singletonList(uuid));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity updateAsset(@PathVariable UUID uuid, @RequestBody CoreAssetDto asset) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.UPDATE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.updateAssets(Collections.singletonList(aFact.update(uuid, asset)));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.PUT, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity addExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.ADD_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.addExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/link/{uuid}/to/{external_id_type}"
    )
    public ResponseEntity updateExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.UPDATE_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.updateExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }
    @RequestMapping(
            method = RequestMethod.DELETE, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity deleteExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.DELETE_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.deleteExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
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
            method = RequestMethod.GET, value = "/func_loc_path/{path}"
    )
    public ResponseEntity getByPath(@PathVariable String path) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity(asDto(assetReader.getAssetByFuncLocPath(path.replace("+","."))), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/linked_to/{external_id_type}/{external_id}"
    )
    public ResponseEntity getByExternalId(@PathVariable String external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity(asDto(assetReader.getAssetByExternalId(external_id_type, external_id)), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    private CoreAssetDto asDto(CoreAsset asset) {
        final CoreAssetDto dto = new CoreAssetDto();

        dto.setCode(asset.getCode());
        dto.setFunc_loc_path(asset.getFunc_loc_path());
        dto.setAddress(asset.getAddress());
        dto.setAsset_type_code(asset.getAsset_type_code());
        dto.setGeom(asset.getGeometry());
        dto.setLongitude(safe(asset.getLongitude()));
        dto.setLatitude(safe(asset.getLatitude()));
        dto.setName(asset.getName());
        dto.setSerial_number(asset.getSerial_number());
        dto.setAdm_path(asset.getAdm_path());
        dto.setBarcode(asset.getBarcode());
        dto.setCreation_date(safe(asset.getCreation_date()));
        dto.setDeactivated_at(safe(asset.getDeactivated_at()));
        dto.setAsset_id(safe(asset.getAsset_id()));
        dto.setIs_owned(asset.getIs_owned() == null ? null : asset.getIs_owned());
        dto.setResponsible_dept_code(asset.getResponsible_dept_code());

        return dto;
    }

    private static String safe(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
}
