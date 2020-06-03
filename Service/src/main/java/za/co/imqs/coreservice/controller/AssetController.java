package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.model.AssetFactory;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.audit.AuditLogEntry.of;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@SuppressWarnings("rawtypes")
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH)
public class AssetController {

    private final CoreAssetWriter assetWriter;

    private final AssetFactory aFact = new AssetFactory();
    private final AuditLoggingProxy audit;

    @Autowired
    public AssetController(
            CoreAssetWriter assetWriter,
            AuditLogger auditLogger
    ) {
        this.assetWriter = assetWriter;
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
}
