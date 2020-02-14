package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import za.co.imqs.formservicebase.interceptors.ServiceFailureInterceptor;
import za.co.imqs.spring.service.auth.AuthInterceptor;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;

/**
 * This is boiler plate stuff. Add entries to the ComponentScan annotation as needed.
 */

@Slf4j
@Configuration
@Profile(PROFILE_PRODUCTION)
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    public static final String ROOT_PATH = "/asset";
    public static final String PING_PATH = ROOT_PATH+"/ping";
    public static final String DIE_PATH = ROOT_PATH+"/die";


    @Autowired
    public AuthInterceptor handleAuthInterceptor;

    @Autowired
    public ServiceFailureInterceptor healthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(healthInterceptor);
        registry.addInterceptor(handleAuthInterceptor).addPathPatterns("/**");
    }
}

