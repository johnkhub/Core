package za.co.imqs.coreservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.common.errors.exception.BadRequestException;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static za.co.imqs.coreservice.WebMvcConfiguration.LOOKUP_ROOT_PATH;
import static za.co.imqs.coreservice.audit.AuditLogEntry.of;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
@SuppressWarnings("rawtypes")
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@RestController
@Slf4j
@RequestMapping(LOOKUP_ROOT_PATH)
public class LookupController {
    private final LookupProvider lookups;
    private final AuditLoggingProxy audit;
    private final ObjectMapper mapper;

    @Autowired
    public LookupController(
            LookupProvider lookups,
            AuditLogger audit,
            ObjectMapper mapper
    ) {
        this.lookups = lookups;
        this.audit = new AuditLoggingProxy(audit);
        this.mapper = mapper;
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{view}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity get(@PathVariable String view, @RequestParam Map<String, String> paramMap) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity(lookups.get(view.replace("+","."), paramMap), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{view}/using_operators",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getWithOperators(
            @PathVariable String view,
            @RequestParam Map<String, String> paramMap

    ) {
        final UserContext user = ThreadLocalUser.get();
        try {
            final Map<String, LookupProvider.Field> fieldsMap = new HashMap<>();
            paramMap.forEach(
                    (k,v) -> {
                        try {
                            fieldsMap.put(k, mapper.readerFor(LookupProvider.Field.class).readValue(v));
                        } catch (Exception e) {
                            throw new BadRequestException(v + " is nor parseable as a field");
                        }
                    });

            return new ResponseEntity(lookups.getWithOperators(view.replace("+","."), fieldsMap), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.GET, value = "/v/{view}/{k}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getKvValue(@PathVariable String view, @PathVariable String k) {
        try {
            final String result = lookups.getKvValue(view.replace("+","."), k);
            if (result != null) {
                return new ResponseEntity(result, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/kv/{view}/{k}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public <T extends LookupProvider.Kv> ResponseEntity getKvRow(@PathVariable String view, @PathVariable String k) {
        try {
            final T result = lookups.getKv(view.replace("+","."), k);
            if (result != null) {
                return new ResponseEntity(result, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.GET, value = "/kv/{view}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getEntireTable(@PathVariable String view) {
        try {
            final List<LookupProvider.Kv> result = lookups.getEntireKvTable(view.replace("+","."));
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/kv",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getKVTypes() {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity(lookups.getKvTypes(), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PUT, value = "/kv/{target}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity acceptKv(@PathVariable String target,  @RequestBody List<LookupProvider.Kv> kvs) {
        final UserContext user = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.ADD_KV_VALUE, of("kv_type", target, "kv", kvs)),
                    () -> {
                        lookups.acceptKv(target, kvs);
                        return null;
                    }
            );

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }
}
