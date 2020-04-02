package za.co.imqs.coreservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2015 IMQS Software
 * <p/>
 * User: FrankVR
 * Date: 2015/11/20
 */
@Configuration
@Profile({PROFILE_PRODUCTION,PROFILE_TEST})
@EnableScheduling
public class ServiceConfiguration {

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler sched =  new ThreadPoolTaskScheduler();
        sched.setWaitForTasksToCompleteOnShutdown(true);
        return sched;
    }
}
