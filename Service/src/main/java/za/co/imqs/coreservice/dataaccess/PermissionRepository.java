package za.co.imqs.coreservice.dataaccess;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;

import java.util.List;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/10
 */
@Repository
public interface PermissionRepository {

    List<UserDto> getUsers();
    List<GroupDto> getGroups();
    void deleteUser(String name);
    void deleteGroup(String name);
    void addGroup(GroupDto group);
    void addUser(UserDto user);

/*
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
*/
    void grantPermissions(UUID entity, UUID principal, int permissions);
    void revokePermissions(UUID entity, UUID principal);
}
