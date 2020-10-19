package za.co.imqs.coreservice;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import za.co.imqs.configuration.client.ConfigClient;

import javax.sql.DataSource;

import java.sql.Connection;

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
@EnableConfigurationProperties(ProjectInfoProperties.class)
@Slf4j
public class ServiceConfiguration {

    // Yeah, this is mutton dressed as lamb, but it starts us down the road of making use of Togglz
    // At least the interface of checking if a feature is enabled sprinkled throughout the code remains the same
    public enum Features implements Feature {

        @Label("Global switch to turn authentication on / off")
        AUTHENTICATION_GLOBAL(true),

        @Label("Global switch to turn authorisation on / off")
        AUTHORISATION_GLOBAL(false),

        @Label("Global switch to turn audit logging on / off")
        AUDIT_GLOBAL(true),

        @Label("Enable the command line option to sync schemas - see SchemaManagment.java")
        SCHEMA_MGMT_SYNC(true),

        @Label("Enable the command line option allow documenting database schemas - see SchemaManagment.java")
        SCHEMA_MGMT_DOC(false),

        @Label("Enable the command line option allow comparing a remote database within the current schemas - see SchemaManagment.java")
        SCHEMA_MGMT_COMPARE(true),

        @Label("Enable to stop liquibase form managing the schema - see SchemaManagment.java")
        SCHEMA_MGMT_SUPPRESS(true);

        private final boolean enabled;

        Features(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isActive() {
            //return FeatureContext.getFeatureManager().isActive(this);
            return enabled;
        }
    }

    private final ConfigClient configClient;
    private final DataSource ds;

    @Autowired
    public ServiceConfiguration(
            ConfigClient configClient,
            @Qualifier("core_ds") DataSource ds
    ) {
        this.configClient = configClient;
        this.ds = ds;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler sched =  new ThreadPoolTaskScheduler();
        sched.setWaitForTasksToCompleteOnShutdown(true);
        sched.initialize();

        for (ScheduledSql s : (ScheduledSql[])configClient.getObject("sql-schedules", ScheduledSql[].class)) {
            sched.schedule(new SqlTask(s.getName(), s.getSql(), ds), new CronTrigger(s.getCron()));
        }

        return sched;
    }

    @Data
    public static class ScheduledSql {
        private String name;
        private String description;
        private String sql;
        private String cron;
    }

    private static class SqlTask implements Runnable {
        private final String name;
        private final String sql;
        private final DataSource ds;

        public SqlTask(String name, String sql, DataSource ds) {
            this.ds = ds;
            this.name = name;
            this.sql = sql;
        }

        @Override
        public void run() {
            log.info("Executing {}", name);
            try(Connection c = ds.getConnection()) {
                // TODO this must be made transactional - probably inject a transaction template rather than ds
                c.prepareStatement(sql).execute();
            } catch (Exception e) {
                log.error("Task {}={} failed to execute: ", name, sql, e);
            }
        }
    }
}
