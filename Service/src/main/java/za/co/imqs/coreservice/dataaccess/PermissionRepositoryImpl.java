package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/17
 */
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@Repository
public class PermissionRepositoryImpl implements PermissionRepository, ApplicationListener<ContextRefreshedEvent> {

    private final JdbcTemplate jdbc;
    private final HashMap<Integer,String> access_types = new HashMap<>();

    private UUID systemPrincipal = null;

    @Autowired
    public PermissionRepositoryImpl(
            @Qualifier("auth_ds") DataSource ds
    ) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    public UUID getSystemPrincipal() {
        try {
            if (systemPrincipal == null) {
                systemPrincipal = retrieveSystemPrincipal();
            }
            return systemPrincipal;
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public List<UserDto> getUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional("auth_tx_mgr")
    public List<GroupDto> getGroups() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional("auth_tx_mgr")
    public GroupDto getGroupByName(String name) {
        return jdbc.queryForObject(
            "SELECT * FROM access_control.principal WHERE name = ?",
                (resultSet, i) -> {
                    final GroupDto g = new GroupDto();
                    g.setGroup_id(UUID.fromString(resultSet.getString("principal_id")));
                    g.setName(resultSet.getString("name"));
                    g.setDescription(resultSet.getString("description"));
                    return g;
                },
                name
        );
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void deleteUser(UUID name) {
        try {
            jdbc.execute("{call access_control.sp_remove_user()}", (CallableStatement stmt) -> {
                stmt.setObject(1, name);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void deleteGroup(String name) {
        try {
            jdbc.execute("{call access_control.sp_remove_group()}", (CallableStatement stmt) -> {
                stmt.setString(1, name);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public UUID addGroup(GroupDto group) {
        try {
            return jdbc.execute("{? = call access_control.sp_add_group(?)}", (CallableStatement stmt) -> {
                stmt.setString(2, group.getName());
                stmt.registerOutParameter(1, Types.OTHER);
                stmt.execute();
                return (UUID)stmt.getObject(1);
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void addUser(UserDto user) {
        try {
            jdbc.execute("{call access_control.sp_add_user(?,?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, user.getPrincipal_id());
                stmt.setString(2, user.getName());
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void addUserToGroup(UUID userId, String groupName) {
        try {
            jdbc.execute("{call access_control.sp_add_user_to_group(?,?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, userId);
                stmt.setString(2, groupName);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void removeUserFromGroup(UUID userId, String groupName) {
        try {
            jdbc.execute("{call access_control.sp_remove_user_group(?,?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, userId);
                stmt.setString(2, groupName);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }


    @Override
    @Transactional("auth_tx_mgr")
    public void grantPermissions(UUID grantor, UUID grantee, int permissions, UUID entity) {
        try {
            jdbc.execute("{call access_control.sp_grant_access(?::uuid,?,?::uuid[],?::uuid)}", (CallableStatement stmt) -> {
                stmt.setObject(1, grantor);
                stmt.setInt(2, permissions);
                stmt.setArray(3, stmt.getConnection().createArrayOf("uuid", new UUID[]{entity}));
                stmt.setObject(4, grantee);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    @Transactional("auth_tx_mgr")
    public void revokePermissions(UUID revoker, UUID revokee, UUID entity) {
        try {
            jdbc.execute("{call access_control.sp_revoke_access(?,?,?,?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, revoker);
                stmt.setObject(2, stmt.getConnection().createArrayOf("uuid", new UUID[]{entity}));
                stmt.setObject(3, revokee);
                stmt.execute();
                return null;
            });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Transactional("auth_tx_mgr")
    public int getPermission(UUID principal, UUID entity) {
        try {
            return jdbc.queryForObject("SELECT access_control.fn_get_effective_access(?,?)", Integer.class, principal, entity);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Transactional("auth_tx_mgr")
    public int getGrant(UUID principal, UUID entity) {
        try {
            return jdbc.queryForObject("SELECT access_control.fn_get_effective_grant(?,?)", Integer.class, principal, entity);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    public List<GroupDto> getGroupsBelongsTo(UUID user) {
        return jdbc.query(
            "SELECT * FROM access_control.principal p JOIN access_control.principal g ON p.group_id = g.id WHERE p.principal_id = ?",
                (resultSet, i) -> {
                    final GroupDto g = new GroupDto();
                    g.setGroup_id(UUID.fromString(resultSet.getString("g.principal_id")));
                    g.setName(resultSet.getString("g.name"));
                    g.setDescription(resultSet.getString("g.description"));
                    return g;
                },
                user
        );
    }

    @Override
    public String getAccessTypeName(int mask) {
        return access_types.get(mask);
    }

    @Override
    public String printBitset(int i) {
        String s = "";
        for (int b = 1; b < 32; b <<= 1) {
            if ((i & b) == b) {
                s = s + getAccessTypeName(b)+",";
            }
        }
        return s.substring(0,s.length()-1);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        jdbc.query("SELECT * FROM access_control.access_type",
                (rs,i) -> {
                    access_types.put(rs.getInt("mask"), rs.getString("name"));
                    return null;
                }
        );
    }

    public UUID retrieveSystemPrincipal() {
        return jdbc.execute("{? = call access_control.fn_get_system_user()}", (CallableStatement stmt) -> {
            stmt.registerOutParameter(1, Types.OTHER);
            stmt.execute();
            return (UUID) stmt.getObject(1);
        });
    }
}
