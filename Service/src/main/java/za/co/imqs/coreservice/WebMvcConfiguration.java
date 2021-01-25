package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import za.co.imqs.formservicebase.interceptors.ServiceFailureInterceptor;
import za.co.imqs.spring.service.auth.AuthInterceptor;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * This is boiler plate stuff. Add entries to the ComponentScan annotation as needed.
 */

@Slf4j
@Configuration
@EnableWebMvc // THIS IS IMPORTANT without it the interceptor filtering does not work
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, WebMvcConfiguration.PROFILE_ADMIN})
public class WebMvcConfiguration implements WebMvcConfigurer {
    public static final String PROFILE_ADMIN = "admin";
    public static final String ASSET_ROOT_PATH = "/assets";
    public static final String LOOKUP_ROOT_PATH = "/lookups";
    public static final String ACCESS_ROOT_PATH =  ASSET_ROOT_PATH + "/access";
    public static final String ACCESS_TESTING_ROOT_PATH =  ASSET_ROOT_PATH + "/access/testing";

    public static final String PING_PATH = ASSET_ROOT_PATH +"/ping";
    public static final String DOWNLOAD_PATH = "/download";

    public static final String DIE_PATH = ASSET_ROOT_PATH +"/die";
    public static final String ASSET_TESTING_PATH = ASSET_ROOT_PATH +"/testing";


    private final AuthInterceptor handleAuthInterceptor;
    private final ServiceFailureInterceptor healthInterceptor;

    @Autowired
    public WebMvcConfiguration(
            AuthInterceptor handleAuthInterceptor,
            ServiceFailureInterceptor healthInterceptor
    ) {
        this.handleAuthInterceptor = handleAuthInterceptor;
        this.healthInterceptor = healthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(healthInterceptor);
        registry.addInterceptor(handleAuthInterceptor).
                addPathPatterns(ASSET_ROOT_PATH+"/**").
                addPathPatterns(LOOKUP_ROOT_PATH+"/**").
                excludePathPatterns(PING_PATH).
                excludePathPatterns(DOWNLOAD_PATH+"/importer/**").
                excludePathPatterns(ASSET_ROOT_PATH+"/meta/viewdependencies");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(DOWNLOAD_PATH+"/importer/**").addResourceLocations("file:"+System.getProperty("user.dir")+"/");
    }
}

