package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.util.UUID;

import static za.co.imqs.coreservice.audit.AuditLogEntry.of;
import static za.co.imqs.coreservice.WebMvcConfiguration.ACCESS_ROOT_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.GET, value= "/user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getUsers() {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            return new ResponseEntity(
                    audit.tryIt(
                            new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.QUERY_USERS, null),
                            () -> permissions.getUsers()
                    ),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.GET, value= "/group",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getGroups() {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            return new ResponseEntity(
                    audit.tryIt(
                            new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.QUERY_GROUPS, null),
                        () ->  permissions.getGroups()
                    ),
                    HttpStatus.OK

            );
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method= RequestMethod.GET, value= "/group/{name}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getGroupByName(@PathVariable String name) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            return new ResponseEntity(
                    audit.tryIt(
                            new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.QUERY_GROUPS, null),
                            () ->  permissions.getGroupByName(name)
                    ),
                    HttpStatus.OK

            );
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.DELETE, value= "/user/{uuid}"
    )
    public ResponseEntity deleteUser(@PathVariable UUID uuid) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
                audit.tryIt(
                        new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.DELETE_USER, of("user", uuid)),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/{name}"
    )
    public ResponseEntity deleteGroup(@PathVariable String name) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.DELETE_GROUP, of("group", name)),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.POST, value= "/group",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addGroup(@RequestBody GroupDto group) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.ADD_GROUP, of("group", group.getGroup_id())),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.POST, value= "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addUser(@RequestBody UserDto user) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.ADD_GROUP, of("group", user.getPrincipal_id())),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.POST, value= "/group/{groupname}/{user_id}"
    )
    public ResponseEntity addUserToGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.JOIN_GROUP, of("group", groupname, "user", user_id)),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/{groupname}/{user_id}"
    )
    public ResponseEntity removeUserFromGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.LEAVE_GROUP, of("group", groupname, "user", user_id)),
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

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.POST, value= "/authorisation/entity/{entity_id}/user/{grantee}/permissions/{perms}"
    )
    public ResponseEntity grantPermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID grantee,
            @PathVariable int perms
    ) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.GRANT_ACL, of("entity", entity_id, "permissions", perms, "grantee", grantee)),
                    () -> {
                        permissions.grantPermissions(invokingUser.getUserUuid(), grantee, perms, entity_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(
            method= RequestMethod.DELETE, value= "/authorisation/entity/{entity_id}"
    )
    public ResponseEntity revokePermissions(
            @PathVariable UUID entity_id,
            @PathVariable UUID revokee
    ) {
        final UserContext invokingUser = ThreadLocalUser.get();
        try {
            audit.tryIt(
                    new AuditLogEntry(invokingUser.getUserUuid(), AuditLogger.Operation.REVOKE_ACL, of("entity", entity_id, "revokee", revokee)),
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
}
