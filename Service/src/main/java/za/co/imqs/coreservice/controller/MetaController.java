package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.FDW_Builder;
import za.co.imqs.coreservice.dataaccess.Meta;
import za.co.imqs.coreservice.dataaccess.MetaImpl;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import javax.sql.DataSource;

import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH+"/meta")
public class MetaController {

    private final Meta meta;
    private static final String[] PREAMBLE = {
            "-- We are very restrictive  in how the fdw is constructed",
            "-- 1. We force the use of the 'normal_reader' role as it is read-only and has restricted visibility",
            "-- 2. We force all local proxies of remote tables into the local public schema"
    };

    @Autowired

    public MetaController(
            @Qualifier("core_ds") DataSource core
    ) {
        this.meta = new MetaImpl(core);
    }

    // e.g. http://localhost:8669/assets/meta/fdw/alias/core_host/username/core_user
    @RequestMapping(
            method = RequestMethod.GET, value = "/fdw/alias/{serveralias}/username/{username}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getFdwDefinition(
            @PathVariable String serveralias,
            @PathVariable String username
    ) {
        try {
            final FDW_Builder bob = new FDW_Builder("normal_reader", "*******",  meta);
            bob.preamble(PREAMBLE).createServer(serveralias).asUser(username);
            return new ResponseEntity(bob.get(), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }
}