package za.co.imqs.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import za.co.imqs.coreservice.auth.authorization.AuthorizationImpl;
import za.co.imqs.formservicebase.workflowhost.UserContextImpl;
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
        if (ServiceConfiguration.Features.AUTHORISATION_GLOBAL.isActive()) {
            return new DefaultHandleAuthInterceptor(
                    authentication(),
                    authorization(),
                    new UserContextFactoryImpl()
            ) {};

        } else {
            return new MockAuthInterceptor(); // TODO instead just use the code we have to create users and create aUUID user
        }
    }

    @Bean
    public Authorization authorization() {
        return new AuthorizationImpl();
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
        return Boolean.valueOf(System.getProperty("in.container", "false")) ? "router" : super.getRouterHost();
    }

    @Override
    protected URL getAuthURL() {
        return applyOverride("authService", super.getAuthURL());
    }


    // TODO move to general code for use wih all services
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

    private static class MockAuthInterceptor extends HandlerInterceptorAdapter implements AuthInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            final UserContext uCtx = new UserContextImpl("cookie", "tenant", UUID.randomUUID().toString(), Collections.emptyList());
            ThreadLocalUser.set(uCtx);
            return true;
        }
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
}
