package za.co.imqs.coreservice.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.common.service.status.ServiceStatus;

import javax.servlet.http.HttpServletResponse;
import static za.co.imqs.coreservice.WebMvcConfiguration.PING_PATH;
import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: NonzalisekoP
 * Date: 12/07/2016
 *
 */
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@RestController
@RequestMapping(PING_PATH)
public class ServiceStatusController {

    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ServiceStatus getStatus(HttpServletResponse response) {
        ServiceStatus.setResponseHeaders(response);
        return new ServiceStatus();
    }

}
