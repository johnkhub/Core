package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/17
 */
@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public PermissionRepositoryImpl(
            @Qualifier("auth_ds") DataSource ds
    ) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    public UUID getSystemPrincipal() {
        return jdbc.execute("{? = call access_control.fn_get_system_user()}", (CallableStatement stmt) -> {
            stmt.registerOutParameter(1, Types.OTHER);
            stmt.execute();
            return (UUID)stmt.getObject(1);
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public List<UserDto> getUsers() {
        return null;
    }

    @Override
    @Transactional("auth_tx_mgr")
    public List<GroupDto> getGroups() {
        return null;
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void deleteUser(UUID name) {
        jdbc.execute("{call access_control.sp_remove_user()}", (CallableStatement stmt) -> {
            stmt.setObject(1, name);
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void deleteGroup(String name) {
        jdbc.execute("{call access_control.sp_remove_group()}", (CallableStatement stmt) -> {
            stmt.setString(1, name);
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public UUID addGroup(GroupDto group) {
        return jdbc.execute("{? = call access_control.sp_add_group(?)}", (CallableStatement stmt) -> {
            stmt.setString(2, group.getName());
            stmt.registerOutParameter(1, Types.OTHER);
            stmt.execute();
            return (UUID)stmt.getObject(1);
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void addUser(UserDto user) {
        jdbc.execute("{call access_control.sp_add_user(?,?)}", (CallableStatement stmt) -> {
            stmt.setObject(1, user.getPrincipal_id());
            stmt.setString(2, user.getName());
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void addUserToGroup(UUID userId, String groupName) {
        jdbc.execute("{call access_control.sp_add_user_to_group(?,?)}", (CallableStatement stmt) -> {
            stmt.setObject(1, userId);
            stmt.setString(2, groupName);
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void removeUserFromGroup(UUID userId, String groupName) {
        jdbc.execute("{call access_control.sp_remove_user_group(?,?)}", (CallableStatement stmt) -> {
            stmt.setObject(1, userId);
            stmt.setString(2, groupName);
            stmt.execute();
            return null;
        });
    }


    @Override
    @Transactional("auth_tx_mgr")
    public void grantPermissions(UUID grantor, UUID grantee, int permissions, UUID entity) {
        jdbc.execute("{call access_control.sp_grant_access(?::uuid,?,?::uuid[],?::uuid)}", (CallableStatement stmt) -> {
            stmt.setObject(1, grantor);
            stmt.setInt(2, permissions);
            stmt.setArray(3, stmt.getConnection().createArrayOf("uuid", new UUID[]{entity}));
            stmt.setObject(4, grantee);
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void revokePermissions(UUID revoker, UUID revokee, UUID entity) {
        jdbc.execute("{call access_control.sp_revoke_access(?,?,?,?)}", (CallableStatement stmt) -> {
            stmt.setObject(1, revoker);
            stmt.setObject(2, stmt.getConnection().createArrayOf("uuid", new UUID[]{entity}));
            stmt.setObject(3, revokee);
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional("auth_tx_mgr")
    public int getPermission(UUID principal, UUID entity) {
        return jdbc.queryForObject("SELECT access_control.fn_get_effective_access(?,?)", Integer.class, principal, entity);
    }

    @Override
    @Transactional("auth_tx_mgr")
    public int getGrant(UUID principal, UUID entity) {
        return jdbc.queryForObject("SELECT access_control.fn_get_effective_grant(?,?)", Integer.class, principal, entity);
    }
}
