package za.co.imqs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import za.co.imqs.unit.dataaccess.DbCreator;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dataaccess.PermissionRepositoryImpl;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.dto.UserDto;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import javax.sql.DataSource;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static za.co.imqs.TestUtils.ServiceRegistry.PG;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
public class TestUtils {
    public static final boolean IS_IN_CONTAINER = false; // test itself is inside a container
    public static final boolean IS_CONNECT_TO_CONTAINER = false; // test is connecting to a service inside a container

    public static final String USERNAME = IS_CONNECT_TO_CONTAINER || IS_IN_CONTAINER ? "demo" : "demo";
    public static final String PASSWORD = IS_CONNECT_TO_CONTAINER || IS_IN_CONTAINER ? "demo" : "demo";
    public static final int CORE_PORT = IS_IN_CONTAINER ?  80 : 8669;


    public static final Environment BOING = new Environment() {
        @Override
        public String[] getActiveProfiles() {
            return new String[0];
        }

        @Override
        public String[] getDefaultProfiles() {
            return new String[0];
        }

        @Override
        public boolean acceptsProfiles(String... strings) {
            return false;
        }

        @Override
        public boolean acceptsProfiles(Profiles profiles) {
            return false;
        }

        @Override
        public boolean containsProperty(String s) {
            return false;
        }

        @Override
        public String getProperty(String s) {
            return null;
        }

        @Override
        public String getProperty(String s, String s1) {
            return null;
        }

        @Override
        public <T> T getProperty(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public <T> T getProperty(String s, Class<T> aClass, T t) {
            return null;
        }

        @Override
        public String getRequiredProperty(String s) throws IllegalStateException {
            return null;
        }

        @Override
        public <T> T getRequiredProperty(String s, Class<T> aClass) throws IllegalStateException {
            return null;
        }

        @Override
        public String resolvePlaceholders(String s) {
            return null;
        }

        @Override
        public String resolveRequiredPlaceholders(String s) throws IllegalArgumentException {
            return null;
        }
    };

    public static final ServiceRegistry SERVICES = new ServiceRegistry();

    public static String getAuthSession(String username, String password)  {
        try {
            HttpClient client = new HttpClient(new SimpleHttpConnectionManager());
            PostMethod post = new PostMethod("http://"+"legendqa" + "/auth2/login");
            post.setRequestHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            client.executeMethod(post);
            if (post.getStatusCode() != 200)
                throw new RuntimeException(String.format("Unable to log in to local IMQS instance with username %s (%s, %s)", username, post.getStatusCode(), new String(post.getResponseBody())));
            return post.getResponseHeader("Set-Cookie").getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * (c) 2020 IMQS Software
     * <p>
     * User: frankvr
     * Date: 2020/02/17
     */
    @Slf4j
    public static class PermissionRepositoryImplTest {
        private final JdbcTemplate jdbc;


        public PermissionRepositoryImplTest() {
            final DataSource ds = HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                    "jdbc:postgresql://"+SERVICES.get(PG)+":5432/test_core","postgres","1mq5p@55w0rd"
            );
            this.jdbc = new JdbcTemplate(ds);

            DbCreator.create(ds);
        }


        @Before
        public void before() {
            jdbc.update("DELETE FROM access_control.entity_access");
            jdbc.update("DELETE FROM access_control.principal");

            // Add a system user
            final UUID system = UUID.randomUUID();
            log.info("Adding SYSTEM Principal {}", system.toString());
            jdbc.update("INSERT INTO access_control.principal (principal_id, group_id, name, description, is_group, reserved) VALUES (?, null, 'System', 'System user is the root grantor of permissions.', false, true)", system);
        }

        @Test
        public void testGrantViaGroup() {
            final PermissionRepository perm = new PermissionRepositoryImpl(jdbc.getDataSource());

            final GroupDto group = new GroupDto();
            group.setName("G1");
            group.setGroup_id(perm.addGroup(group));

            final UserDto user = new UserDto();
            user.setName("U1");
            user.setPrincipal_id(UUID.randomUUID());
            perm.addUser(user);

            perm.addUserToGroup(user.getPrincipal_id(), "G1");

            final UUID entity = UUID.randomUUID();
            perm.grantPermissions(perm.getSystemPrincipal(), group.getGroup_id(), 2, entity);

            assertEquals(2, perm.getPermission(user.getPrincipal_id(), entity));
        }

        @Test
        public void testGrantToUser() {
            final PermissionRepository perm = new PermissionRepositoryImpl(jdbc.getDataSource());

            final UserDto user = new UserDto();
            user.setName("U1");
            user.setPrincipal_id(UUID.randomUUID());
            perm.addUser(user);

            final UUID entity = UUID.randomUUID();
            perm.grantPermissions(perm.getSystemPrincipal(), user.getPrincipal_id(), 2, entity);

            assertEquals(2, perm.getPermission(user.getPrincipal_id(), entity));
        }

        @Test
        public void testGrantNotOwned() {
            fail("Not implemented");
        }

        @Test
        public void testGrantToNonExistantUser() {
            fail("Not implemented");
        }

        @Test
        public void testGrantByNonExistantUser() {
            fail("Not implemented");
        }

        @Test
        public void testRevoke() {
            final PermissionRepository perm = new PermissionRepositoryImpl(jdbc.getDataSource());

            final GroupDto group = new GroupDto();
            group.setName("G1");
            group.setGroup_id(UUID.randomUUID());
            perm.addGroup(group);

            final UserDto user = new UserDto();
            user.setName("U1");
            user.setPrincipal_id(UUID.randomUUID());
            perm.addUser(user);

            final UUID entity = UUID.randomUUID();
            perm.grantPermissions(perm.getSystemPrincipal(), user.getPrincipal_id(), 2, entity);

            assertEquals(2, perm.getGrant(user.getPrincipal_id(), entity));


            perm.revokePermissions(perm.getSystemPrincipal(), user.getPrincipal_id(), entity);

            assertEquals(0, perm.getGrant(user.getPrincipal_id(), entity));
        }

        @Test
        public void testRevokeNotOwned() {
            fail("Not implemented");
        }

        @Test
        public void testRevokeFromNonExistantUser() {
            fail("Not implemented");
        }

        @Test
        public void testGetGrant() {
            fail("Not implemented");
        }


        @Test
        @Ignore("Implicitly tested")
        public void testGetAccess() {
            fail("Not implemented");
        }

    }

    public static class ServiceRegistry {
        public static final String AUTH = "auth";
        public static final String ROUTER = "router";
        public static final String CONFIG = "config";
        public static final String CORE = "asset-core-service";
        public static final String PG = "db";
        public static final String MS = "mssql";

        private final Map<String,String> m = new HashMap<>();

        public ServiceRegistry() {
            m.put(AUTH, AUTH);
            m.put(ROUTER, ROUTER);
            m.put(PG,PG);
            m.put(CONFIG, CONFIG);
            m.put(CORE, CORE);
            m.put(MS, MS);
        }

        public String get(String name) {
            return IS_IN_CONTAINER ? m.get(name)  : "localhost";
        }
    }
}
