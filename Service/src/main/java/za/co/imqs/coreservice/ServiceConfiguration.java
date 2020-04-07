package za.co.imqs.coreservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

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

    // Yeah, this is mutton dressed as lamb, but it starts us down the road of making use of Togglz
    // At least the interface of checking if a feature is enabled sprinkled throughout the code remains the same
    public enum Features implements Feature {
        @Label("Global switch to turn authorisation on / off")
        AUTHORISATION_GLOBAL(false),

        @Label("Global switch to turn audit logging on / off")
        AUDIT_GLOBAL(false),

        @Label("Enable the command line option to sync schemas - see SchemaManagment.java")
        SCHEMA_MGMT_SYNC(false),

        @Label("Enable the command line option allow documenting database schemas - see SchemaManagment.java")
        SCHEMA_MGMT_DOC(false),

        @Label("Enable the command line option allow comparing a remote database within the current schemas - see SchemaManagment.java")
        SCHEMA_MGMT_COMPARE(false);

        private final boolean enabled;

        Features(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isActive() {
            //return FeatureContext.getFeatureManager().isActive(this);
            return enabled;
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler sched =  new ThreadPoolTaskScheduler();
        sched.setWaitForTasksToCompleteOnShutdown(true);
        return sched;
    }
}
