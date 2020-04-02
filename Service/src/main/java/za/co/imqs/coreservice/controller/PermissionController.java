package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dataaccess.exception.*;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;
import za.co.imqs.spring.service.auth.ThreadLocalUser;
import za.co.imqs.spring.service.auth.authorization.UserContext;

import java.util.UUID;

import static za.co.imqs.coreservice.WebMvcConfiguration.ACCESS_ROOT_PATH;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@RestController
@Slf4j
@RequestMapping(ACCESS_ROOT_PATH)
public class PermissionController {
    private final PermissionRepository permissions;
    private final AuditLoggingProxy audit;

    @Autowired
    public PermissionController(
            AuditLogger auditLogger,
            PermissionRepository permissions
    ) {
        this.audit = new AuditLoggingProxy(auditLogger);
        this.permissions = permissions;
    }

    @RequestMapping(
            method= RequestMethod.GET, value= "/user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getUsers() {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            return new ResponseEntity(
                    audit.tryIt(
                        new AuditLogEntry(invokingUser.getUserId(), "", "", AuditLogger.Operation.ADD, ""),
                        () -> permissions.getUsers()
                    ),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.GET, value= "/group",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getGroups() {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            return new ResponseEntity(
                    audit.tryIt(
                        new AuditLogEntry(invokingUser.getUserId(), "", "", AuditLogger.Operation.ADD, ""),
                        () ->  permissions.getGroups()
                    ),
                    HttpStatus.OK

            );
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/user/{uuid}"
    )
    public ResponseEntity deleteUser(@PathVariable UUID uuid) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
                audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "user", "", AuditLogger.Operation.DELETE, ""),
                    () -> {
                        permissions.deleteUser(uuid);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/name"
    )
    public ResponseEntity deleteGroup(@PathVariable String name) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "group", "", AuditLogger.Operation.DELETE, ""),
                    () -> {
                        permissions.deleteGroup(name);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/group",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addGroup(@RequestBody GroupDto group) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "group", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        permissions.addGroup(group);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addUser(@RequestBody UserDto user) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "user", "", AuditLogger.Operation.ADD, ""),
                    () -> {
                        permissions.addUser(user);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/group/{groupname}/{user_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addUserToGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "group", user_id.toString(), AuditLogger.Operation.ADD, ""),
                    () -> {
                        permissions.addUserToGroup(user_id, groupname);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/{groupname}/{user_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity removeUserFromGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "group", user_id.toString(), AuditLogger.Operation.DELETE, ""),
                    () -> {
                        permissions.removeUserFromGroup(user_id, groupname);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/authorisation/entity/{entity_id}/user/{grantee}/permissions/{perms}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity grantPermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID grantee,
            @PathVariable int perms
    ) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "", entity_id.toString(), AuditLogger.Operation.GRANT, ""),
                    () -> {
                        permissions.grantPermissions(UUID.fromString(invokingUser.getUserId()), grantee, perms, entity_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/authorisation/entity/{entity_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity revokePermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID revokee
    ) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserId(), "", entity_id.toString(), AuditLogger.Operation.REVOKE, ""),
                    () -> {
                        permissions.revokePermissions(UUID.fromString(invokingUser.getUserId()), revokee, entity_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    private ResponseEntity mapException(Exception exception) {
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
