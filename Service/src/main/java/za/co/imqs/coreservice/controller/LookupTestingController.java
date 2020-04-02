package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.*;
import za.co.imqs.spring.service.auth.ThreadLocalUser;
import za.co.imqs.spring.service.auth.authorization.UserContext;

import static za.co.imqs.coreservice.WebMvcConfiguration.LOOKUP_ROOT_PATH;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_TESTING_PATH;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 */
@Profile(PROFILE_TEST)
@RestController
@Slf4j
@RequestMapping(ASSET_TESTING_PATH +LOOKUP_ROOT_PATH)
public class LookupTestingController {

    private final LookupProvider lookups;


    @Autowired
    public LookupTestingController(
            LookupProvider lookups
    ) {
        this.lookups = lookups;
    }


    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{target}"
    )
    public ResponseEntity delete(@PathVariable String target) {
        try {
            final UserContext user = ThreadLocalUser.get();
            lookups.obliterateKv(target);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    private ResponseEntity mapException(Exception exception) {
        log.error("--> ",exception);
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
