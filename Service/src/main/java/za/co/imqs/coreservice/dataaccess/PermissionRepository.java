package za.co.imqs.coreservice.dataaccess;

import za.co.imqs.coreservice.dataaccess.exception.NotPermittedException;
import za.co.imqs.coreservice.dto.auth.GroupDto;
import za.co.imqs.coreservice.dto.auth.UserDto;

import java.util.List;
import java.util.UUID;

import static za.co.imqs.coreservice.Boot.Features.AUTHORISATION_GLOBAL;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/10
 */
public interface PermissionRepository {
    static final String SYSTEM = "System";

    static final int PERM_NONE = 0;
    static final int PERM_CREATE = 1;
    static final int PERM_READ = 2;
    static final int PERM_UPDATE = 4;
    static final int PERM_DELETE = 8;


    UUID getSystemPrincipal();

    List<UserDto> getUsers();
    List<GroupDto> getGroups();
    GroupDto getGroupByName(String name);

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
    public List<GroupDto> getGroupsBelongsTo(UUID user);

    String getAccessTypeName(int mask);
    String printBitset(int i);

    default void expectPermission(UUID principal, UUID entity, int required) {
        if (AUTHORISATION_GLOBAL.isActive()) {
            int found = required & getPermission(principal, entity);
            if (found != required) {
                throw new NotPermittedException(
                        String.format(
                                "User %s does not have permissions [%s] for  %s",
                                principal.toString(), printBitset(~found & required), principal.toString()
                        )
                );
            }
        }
    }
}
