package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;

import java.util.UUID;

import static za.co.imqs.coreservice.WebMvcConfiguration.ACCESS_TESTING_ROOT_PATH;
import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@RestController
@Slf4j
@RequestMapping(ACCESS_TESTING_ROOT_PATH)
public class PermissionTestingController {
    private final PermissionRepository permissions;

    @Autowired
    public PermissionTestingController(
            PermissionRepository permissions
    ) {
        this.permissions = permissions;
    }



    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.POST, value= "/authorisation/entity/{entity_id}/user/{grantee}/permissions/{perms}"
    )
    public ResponseEntity grantPermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID grantee,
            @PathVariable int perms
    ) {
        permissions.grantPermissions(permissions.getSystemPrincipal(), grantee, perms, entity_id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.DELETE, value= "/authorisation/entity/{entity_id}"
    )
    public ResponseEntity revokePermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID revokee
    ) {
        permissions.revokePermissions(permissions.getSystemPrincipal(), revokee, entity_id);
        return new ResponseEntity(HttpStatus.OK);

    }
}
