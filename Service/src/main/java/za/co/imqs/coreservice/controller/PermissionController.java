package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;

import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@RestController
@Slf4j
@RequestMapping("/assets/access")
public class PermissionController {

    private final PermissionRepository permissions;

    @Autowired
    public PermissionController(PermissionRepository permissions) {
        this.permissions = permissions;
    }

    @RequestMapping(
            method= RequestMethod.GET, value= "/user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getUsers() {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.GET, value= "/group",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getGroups() {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/user/{uuid}"
    )
    public ResponseEntity deleteUser() {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/name"
    )
    public ResponseEntity deleteGroup(@PathVariable String name) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/group",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addGroup(@RequestBody GroupDto group) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/user",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addUser(@RequestBody UserDto user) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/group/{groupname}/{user_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addUserToGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/group/{groupname}/{user_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity removeUserFromGroup(@PathVariable String groupname, @PathVariable UUID user_id) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.POST, value= "/authorisation/entity/{entity_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity grantPermissions(@PathVariable UUID entity_id) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            method= RequestMethod.DELETE, value= "/authorisation/entity/{entity_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity revokePermissions(@PathVariable UUID entity_id) {
        return new ResponseEntity(HttpStatus.OK);
    }
}
