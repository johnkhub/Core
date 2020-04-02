package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static za.co.imqs.coreservice.WebMvcConfiguration.LOOKUP_ROOT_PATH;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
@RestController
@Slf4j
@RequestMapping(LOOKUP_ROOT_PATH)
public class LookupController {
    private final LookupProvider lookups;

    @Autowired
    public LookupController(LookupProvider lookups) {
        this.lookups = lookups;
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{view}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Map<String,Object>>> get(@PathVariable String view, @RequestParam Map<String, String> paramMap) {
        try {
            return new ResponseEntity(lookups.get(view.replace("%2E","."), paramMap), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{view}/using_operators",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Map<String,Object>>> getWithOperators(@PathVariable String view, @RequestBody Map<String, LookupProvider.Field> paramMap) {
        try {
            return new ResponseEntity(lookups.getWithOperators(view.replace("%2E","."), paramMap), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.GET, value = "/v/{view}/{k}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Map<String,Object>>> getKv(@PathVariable String view, @PathVariable String k) {
        try {
            final String result = lookups.getKv(view.replace("%2E","."), k);
            if (result != null) {
                return new ResponseEntity(result, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/kv",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LookupProvider.KvDef>> getKVTypes() {
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
        try {
            lookups.acceptKv(target, kvs);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    private ResponseEntity mapException(Exception exception) {
        log.error(exception.getMessage());
        if (exception instanceof AlreadyExistsException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
        } else if (exception instanceof NotFoundException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        } else if (exception instanceof NotPermittedException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.FORBIDDEN);
        } else if (exception instanceof ValidationFailureException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (exception instanceof BusinessRuleViolationException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.PRECONDITION_FAILED);
        } else if (exception instanceof ResubmitException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.REQUEST_TIMEOUT); // So the client can resubmit
        } else {
            final String stacktrace = ExceptionUtils.getStackTrace(exception);
            return new ResponseEntity(exception.getMessage() + "\n" + stacktrace, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
