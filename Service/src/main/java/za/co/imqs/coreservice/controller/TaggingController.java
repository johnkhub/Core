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
import za.co.imqs.coreservice.dataaccess.TagRepository;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.ArrayList;
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
public class TaggingController {

    private final AuditLoggingProxy audit;
    private final TagRepository tagging;

    @Autowired
    public TaggingController(
            AuditLogger auditLogger,
            TagRepository tagging
    ) {
        this.audit = new AuditLoggingProxy(auditLogger);
        this.tagging = tagging;
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{uuid}/tag",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getTags(@PathVariable UUID uuid) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            return new ResponseEntity(
                    audit.tryIt(
                        new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.QUERY_TAGS, of("asset", uuid)).setCorrelationId(uuid),
                        () -> tagging.getTagsFor(uuid)
                    ),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{uuid}/tag/{tag1}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity hasTags(@PathVariable UUID uuid, @PathVariable String tag1, @RequestParam Map<String, String> paramMap) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            final ArrayList<Boolean> result = new ArrayList<>();
            result.add(
                    (Boolean)audit.tryIt(
                        new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.QUERY_TAGS, of("asset", uuid)).setCorrelationId(uuid),
                        () -> {
                            paramMap.put(tag1, null);
                            return tagging.hasTags(uuid, paramMap.keySet().toArray(new String[0]));
                        }
                    )
            );
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PUT, value = "/{uuid}/tag/{tag1}"
    )
    public ResponseEntity addTag(@PathVariable UUID uuid, @PathVariable String tag1, @RequestParam Map<String, String> paramMap) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.ADD_TAGS,
                            of(
                                    "asset", uuid,
                                    "tags", String.join(",", paramMap.keySet())
                            )).setCorrelationId(uuid),
                    () -> {
                        paramMap.put(tag1, null);
                        tagging.addTags(uuid, paramMap.keySet().toArray(new String[0]));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{uuid}/tag/{tag1}"
    )
    public ResponseEntity deleteTag(@PathVariable UUID uuid, @PathVariable String tag1, @RequestParam Map<String, String> paramMap) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(UUID.fromString(user.getUserId()), AuditLogger.Operation.DELETE_TAGS,
                            of(
                                    "asset", uuid,
                                    "tags", String.join(",", paramMap.keySet())
                            )).setCorrelationId(uuid),
                    () -> {
                        paramMap.put(tag1, null);
                        tagging.deleteTags(uuid, paramMap.keySet().toArray(new String[0]));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }
}
