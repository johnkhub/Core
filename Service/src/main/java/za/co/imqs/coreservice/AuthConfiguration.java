package za.co.imqs.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import za.co.imqs.common.security.Permissions;
import za.co.imqs.formservicebase.workflowhost.UserContextImpl;
import za.co.imqs.libimqs.auth.AuthResponse;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;
import za.co.imqs.services.serviceauth.ServiceAuth;
import za.co.imqs.services.serviceauth.ServiceAuthImpl;
import za.co.imqs.spring.service.auth.AuthInterceptor;
import za.co.imqs.spring.service.auth.DefaultHandleAuthInterceptor;
import za.co.imqs.spring.service.auth.authorization.Authorization;
import za.co.imqs.spring.service.auth.authorization.UserContextFactory;
import za.co.imqs.spring.service.factorybeandefinitions.BaseAuthConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static za.co.imqs.coreservice.ServiceConfiguration.Features.AUTHENTICATION_GLOBAL;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2019 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2019/02/15
 */

@Configuration
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@Slf4j
public class AuthConfiguration extends BaseAuthConfiguration {

    private final ObjectMapper mapper;
    private final DataSource ds;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    public AuthConfiguration(
            @Qualifier("core_ds") DataSource ds,
            ObjectMapper mapper
    ) {
        this.ds = ds;
        this.mapper = mapper;
    }

    @Bean
    @Qualifier("auth_ds")
    public DataSource getAuthDataSource() {
        return ds;
    }

    @Bean
    public AuthInterceptor handleAuthInterceptor() {
        if (!AUTHENTICATION_GLOBAL.isActive() || (activeProfile.equals(PROFILE_TEST) && Boolean.valueOf(System.getenv("FAKEAUTH")))) {
            log.warn("AUTHENTICATION HAS BEEN DISABLED!");
            return new AuthInterceptor() {
                final UserContextFactory uCtxFact = new UserContextFactoryImpl();
                final UUID session = UUID.randomUUID();
                UUID user = null;

                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                    if (user == null) {
                        user = new JdbcTemplate(ds).queryForObject("SELECT principal_id FROM access_control.principal WHERE name = 'System'", UUID.class);
                    }
                    ThreadLocalUser.set(
                            uCtxFact.get(session.toString(),"SYSTEM","tenantId", Collections.emptyList(),user));

                    return true;
                }
            };
        }
        return new DefaultHandleAuthInterceptor(
                authentication(),
                new Authorization() {
                    @Override
                    public boolean authorize(AuthResponse authAuthResponse) {
                        return true;
                    }

                    @Override
                    public boolean authorize(UserContext uCtx, String eventId) {
                        return true;
                    }

                    @Override
                    public boolean authorize(UserContext uCtx, Permissions p) {
                        return true;
                    }
                },
                new UserContextFactoryImpl()
        ) {};
    }



    @Bean
    @Qualifier("auth_tx_mgr")
    public PlatformTransactionManager getAuthTransactionManager() {
        return new DataSourceTransactionManager(getAuthDataSource());
    }

    @Bean
    public ServiceAuth getServiceAuth(){
        /*
        final List<PermissionDTO> configAuthGroups =
                ConfigurationUtils.getPropertyListWithClass(
                        configClient, mapper, "Properties.permissionGroups", PermissionDTO.class
                );
        final List<UserDTO> configAuthUsers =
                ConfigurationUtils.getPropertyListWithClass(
                        configClient, mapper, "Properties.users", UserDTO.class
                );

         */
        return new ServiceAuthImpl(getAuthURL(), mapper, Collections.emptyList(), Collections.emptyList());
    }


    @Bean
    @Qualifier("routerHost")
    @Override
    public String getRouterHost() {
        return log("ROUTER=",Boolean.valueOf(System.getProperty("in.container", "false")) ? "router" : super.getRouterHost());
    }

    @Override
    protected URL getAuthURL() {
        return log("AUTH=", applyOverride("authService", super.getAuthURL()));
    }


    // TODO move to general code for use with all services
    private URL applyOverride(String serviceName, URL toOverride) {
        if (configClient.getProperty(serviceName) != null) {
            try {
                return new URL( "http://"+ configClient.getProperty(serviceName) +"/auth2");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return toOverride;
    }

    private static class UserContextFactoryImpl implements UserContextFactory {
        @Override
        public UserContext get(String sessionCookie, String userId, String tenantId, List<String> roles) {
            return get(sessionCookie, userId, tenantId, roles, null);
        }

        @Override
        public UserContext get(String sessionCookie, String userId, String tenantId, List<String> roles, UUID userUuid) {
            return new UserContextImpl(sessionCookie, tenantId, userId, roles, userUuid);
        }
    }

    private <T> T log(String message, T value) {
        log.info(String.format("%s %s",message, value.toString()));
        return value;
    }
}
