package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Retryable;
import za.co.imqs.spring.service.health.ServiceHealth;
import za.co.imqs.spring.service.health.ServiceHealthImpl;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;


@Slf4j
@Configuration
@Retryable
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
public class ServiceHealthConfiguration {

    @Bean
    public ServiceHealth getHealth() {
        final ServiceHealth health = new ServiceHealthImpl();
        Thread.setDefaultUncaughtExceptionHandler(
                (Thread t, Throwable e) ->  health.fail(e)
        );
        return health;
    }
}
