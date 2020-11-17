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
import za.co.imqs.configuration.client.ConfigClient;

import javax.sql.DataSource;

import java.sql.Connection;

import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2015 IMQS Software
 * <p/>
 * User: FrankVR
 * Date: 2015/11/20
 */
@Configuration
@Profile({PROFILE_PRODUCTION,PROFILE_TEST, PROFILE_ADMIN})
@EnableScheduling
@EnableConfigurationProperties(ProjectInfoProperties.class)
@Slf4j
public class ServiceConfiguration {
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

    @Bean
    @Qualifier("visible_host_url")
    public String getVisibleHostUrl() {
        String host = System.getenv("IMQS_HOSTNAME_URL");
        if (host == null) {
            log.warn("IMQS_HOSTNAME_URL environment variable not set.");
            host = System.getenv("COMPUTERNAME");
            if (host != null) {
                return "http://"+host+"/";
            }
            return "http://localhost:8669/";
        }
        return host;
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
                c.setAutoCommit(false);

                try {
                    c.prepareStatement(sql).execute();
                    c.commit();
                } catch (Exception t) {
                    c.rollback();
                }
            } catch (Exception e) {
                log.error("Task {}={} failed to execute: ", name, sql, e);
            }
        }
    }
}
