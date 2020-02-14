package za.co.imqs.coreservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.common.service.status.ServiceStatus;

import javax.servlet.http.HttpServletResponse;
import static za.co.imqs.coreservice.WebMvcConfiguration.PING_PATH;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: NonzalisekoP
 * Date: 12/07/2016
 *
 */
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
