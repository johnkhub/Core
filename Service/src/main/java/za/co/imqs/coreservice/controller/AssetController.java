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
import za.co.imqs.coreservice.dataaccess.exception.*;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.model.CoreAssetFactory;

import java.util.Collections;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@RestController
@Slf4j
@RequestMapping("/assets")
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
            method= RequestMethod.PUT, value= "/",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addAsset(@RequestBody CoreAssetDto asset) {
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                        new AuditLogEntry("", "", "", AuditLogger.Operation.ADD, ""),
                        () -> {
                            assetWriter.createAssets(Collections.singletonList(aFact.from(asset))); return null;
                        }
                    );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/{uuid}"
    )
    public ResponseEntity deleteAsset(@PathVariable String uuid) {
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry("", "", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.deleteAssets(Collections.singletonList(asUUID(uuid))); return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.PATCH, value= "/"
    )
    public ResponseEntity updateAsset(@RequestBody CoreAssetDto asset) {
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry("", "", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.updateAssets(Collections.singletonList(aFact.from(asset))); return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.PUT, value= "asset/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity addExternalLink(@RequestParam String uuid, @RequestParam String external_id_type, @RequestParam String external_id) {
        // Authentication
        // Authorisation
        // Audit logging
        try {
            assetWriter.addExternalLink(asUUID(uuid), external_id_type, external_id);
            audit.tryIt(
                    new AuditLogEntry("", "", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.addExternalLink(asUUID(uuid), external_id_type, external_id); return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "asset/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity deleteExternalLink(@RequestParam String uuid, @RequestParam String external_id_type, @RequestParam String external_id) {
        // Authentication
        // Authorisation
        // Audit logging
        try {
            audit.tryIt(
                    new AuditLogEntry("", "", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        assetWriter.deleteExternalLink(asUUID(uuid), external_id_type, external_id); return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    private ResponseEntity mapException(Exception exception) {
        if (exception instanceof AlreadyExistsException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
        } else if (exception instanceof NotFoundException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        } else if (exception instanceof NotPermittedException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.FORBIDDEN);
        } else if (exception instanceof ValidationFailureException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (exception instanceof ResubmitException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.REQUEST_TIMEOUT); // So the client can resubmit
        } else {
            return new ResponseEntity(exception.getMessage()+"\n"+exception.getStackTrace(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UUID asUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new ValidationFailureException(uuid + " is not a valid UUID");
        }
    }
    /*

```
-- explicit endpoint for each type so we then need to only add endpoints instead of changing -- -- existing code when new types are introduced

```
PUT assets/{uuid}/envelope
{
}
```

```
PUT assets/{uuid}/facility
{
}
```


```
PUT assets/{uuid}/building
{
}
```


```
PUT assets/{uuid}/floor
{
}
```


```
PUT assets/{uuid}/room
{
}
```

```
PUT assets/{uuid}/component
{
    "component_type"
}
```

     */
}
