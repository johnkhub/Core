package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.co.imqs.coreservice.dataaccess.exception.*;

@SuppressWarnings("rawtypes")
@Slf4j
public class ExceptionRemapper {

    public static ResponseEntity<String> mapException(Exception exception) {

        if (exception instanceof AlreadyExistsException) {
            log.error("CONFLICT --> ", exception.getMessage());
            return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
        } else if (exception instanceof NotFoundException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        } else if (exception instanceof NotPermittedException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.FORBIDDEN);
        } else if (exception instanceof ValidationFailureException) {
            log.error("BAD REQUEST --> ", exception.getMessage());
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (exception instanceof BusinessRuleViolationException) {
            log.error("RULE VIOLATION --> ", exception);
            return new ResponseEntity(exception.getMessage(), HttpStatus.PRECONDITION_FAILED);
        } else if (exception instanceof ResubmitException) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.REQUEST_TIMEOUT); // So the client can resubmit
        } else if (exception instanceof ExplicitRollbackException) {
            return new ResponseEntity(HttpStatus.CREATED);
        } else {
            log.error("INTERNAL SERVER ERROR --> ", exception);
            final String stacktrace = ExceptionUtils.getStackTrace(exception);
            return new ResponseEntity(exception.getMessage() + "\n" + stacktrace, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
