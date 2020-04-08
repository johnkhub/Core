package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.dataaccess.LookupProvider;

import java.util.List;
import java.util.Map;

import static za.co.imqs.coreservice.WebMvcConfiguration.LOOKUP_ROOT_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;

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
    public ResponseEntity get(@PathVariable String view, @RequestParam Map<String, String> paramMap) {
        try {
            return new ResponseEntity(lookups.get(view.replace("+","."), paramMap), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/{view}/using_operators",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getWithOperators(@PathVariable String view, @RequestBody Map<String, LookupProvider.Field> paramMap) {
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
    public ResponseEntity getKv(@PathVariable String view, @PathVariable String k) {
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
    public ResponseEntity getKVTypes() {
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
}
