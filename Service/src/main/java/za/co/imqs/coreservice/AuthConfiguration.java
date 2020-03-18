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
import za.co.imqs.coreservice.auth.authorization.AuthorizationImpl;
import za.co.imqs.formservicebase.workflowhost.UserContextImpl;
import za.co.imqs.services.serviceauth.ServiceAuth;
import za.co.imqs.services.serviceauth.ServiceAuthImpl;
import za.co.imqs.spring.service.auth.AuthInterceptor;
import za.co.imqs.spring.service.auth.DefaultHandleAuthInterceptor;
import za.co.imqs.spring.service.auth.authorization.Authorization;
import za.co.imqs.spring.service.factorybeandefinitions.BaseAuthConfiguration;

import javax.sql.DataSource;
import java.util.Collections;

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
    public AuthInterceptor handleAuthInterceptor(){
        return new DefaultHandleAuthInterceptor(
                authentication(),
                authorization(),
                (sessionCookie, userId, tenantId, roles) -> new UserContextImpl(sessionCookie, tenantId, userId, roles)){};
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
        return new ServiceAuthImpl(getHostAndPort(), mapper, Collections.emptyList(), Collections.emptyList());
    }

    @Bean
    @Qualifier("routerPort")
    @Override
    public int getRouterPort() {
        return super.getRouterPort();
    }

    @Bean
    @Qualifier("routerHost")
    @Override
    public String getRouterHost() {
        return Boolean.valueOf(System.getProperty("in.container", "false")) ? "router" : super.getRouterHost();
    }

    private String getHostAndPort() {
        return getRouterPort() == 80 ? getRouterHost() : (getRouterHost()+":"+getRouterPort());
    }
}
