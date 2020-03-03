package za.co.imqs.coreservice.dataaccess;

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
public interface PermissionRepository {
    static final String SYSTEM = "System";

    UUID getSystemPrincipal();

    List<UserDto> getUsers();
    List<GroupDto> getGroups();

    void deleteUser(UUID userId);
    void deleteGroup(String name);
    UUID addGroup(GroupDto group);
    void addUser(UserDto user);


    void addUserToGroup(UUID userId, String groupName);
    void removeUserFromGroup(UUID userId, String groupName);

    void grantPermissions(UUID grantor, UUID grantee, int permissions, UUID entity);
    void revokePermissions(UUID revoker, UUID revokee, UUID entity);
    int getPermission(UUID principal, UUID entity);
    int getGrant(UUID principal, UUID entity);
}
