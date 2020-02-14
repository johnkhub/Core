package za.co.imqs.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import za.co.imqs.coreservice.auth.authorization.AuthorizationImpl;
import za.co.imqs.formservicebase.workflowhost.UserContextImpl;
import za.co.imqs.libimqs.utils.ConfigurationUtils;
import za.co.imqs.services.serviceauth.ServiceAuth;
import za.co.imqs.services.serviceauth.ServiceAuthImpl;
import za.co.imqs.services.serviceauth.dto.PermissionDTO;
import za.co.imqs.services.serviceauth.dto.UserDTO;
import za.co.imqs.spring.service.auth.AuthInterceptor;
import za.co.imqs.spring.service.auth.DefaultHandleAuthInterceptor;
import za.co.imqs.spring.service.auth.authorization.Authorization;
import za.co.imqs.spring.service.factorybeandefinitions.BaseAuthConfiguration;

import java.util.Collections;
import java.util.List;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;

/**
 * (c) 2015 IMQS Software
 * <p/>
 * User: AbramS
 * Date: 2016/08/31
 */

@Configuration
@Profile({PROFILE_PRODUCTION})
@Slf4j
public class AuthConfiguration extends BaseAuthConfiguration {

    @Autowired
    private ObjectMapper mapper;

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

    private String getHostAndPort() {
        return getRouterPort() == 80 ? getRouterHost() : (getRouterHost()+":"+getRouterPort());
    }
}
