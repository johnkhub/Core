package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import za.co.imqs.common.errors.RestResponseExceptionHandler;

/**
 * (c) 2017 IMQS Software
 * <p>
 * User: BradleyMe
 * Date: 15-Feb-18.
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends RestResponseExceptionHandler {
}
