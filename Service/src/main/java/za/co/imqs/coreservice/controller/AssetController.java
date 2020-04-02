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
import za.co.imqs.coreservice.model.CoreAssetFactory;
import za.co.imqs.spring.service.auth.ThreadLocalUser;
import za.co.imqs.spring.service.auth.authorization.UserContext;

import java.util.Collections;

import static za.co.imqs.coreservice.Validation.asUUID;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH)
public class AssetController {

    private final CoreAssetWriter assetWriter;

    private final CoreAssetFactory aFact = new CoreAssetFactory();
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
    public ResponseEntity addAsset(@PathVariable String uuid, @RequestBody CoreAssetDto asset) {
        final UserContext user = ThreadLocalUser.get();
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserId(), "", uuid, AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.createAssets(Collections.singletonList(aFact.create(asUUID(uuid), asset)));
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
    public ResponseEntity deleteAsset(@PathVariable String uuid) {
        final UserContext user = ThreadLocalUser.get();
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserId(), "", uuid, AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.deleteAssets(Collections.singletonList(asUUID(uuid)));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/{uuid}"
    )
    public ResponseEntity updateAsset(@PathVariable String uuid, @RequestBody CoreAssetDto asset) {
        final UserContext user = ThreadLocalUser.get();
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserId(), "", uuid, AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.updateAssets(Collections.singletonList(aFact.update(asUUID(uuid), asset)));
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
    public ResponseEntity addExternalLink(@PathVariable String uuid, @PathVariable String external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserId(), "", uuid, AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.addExternalLink(asUUID(uuid), asUUID(external_id_type), external_id);
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
    public ResponseEntity deleteExternalLink(@PathVariable String uuid, @PathVariable String external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserId(), "", uuid, AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.deleteExternalLink(asUUID(uuid), asUUID(external_id_type), external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }
}
