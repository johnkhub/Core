package za.co.imqs.coreservice;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import za.co.imqs.coreservice.controller.ExportController;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;

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

    @Value("${server.port}")
    private int serverPort;

    private final int routerPort;

    @Autowired
    public ServiceConfiguration(
            ConfigClient configClient,
            @Qualifier("core_ds") DataSource ds,
            @Qualifier("routerPort") int routerPort
    ) {
        this.configClient = configClient;
        this.ds = ds;
        this.routerPort = routerPort;
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
        // 1. For Docker we need to set the IMQS_HOSTNAME_URL there is no alternative
        //    as the host exposed to the outside word is not this one.
        // 2. On Windows we allow fallback to the COMPUTERNAME variable as it is mostly set
        //    and a handy thing for the sandpit scenario
        // 3. Is a last resort that will mostly be the right thing on a native
        //    development instance
        String host = System.getenv("IMQS_HOSTNAME_URL");
        if (host == null) {
            log.warn("IMQS_HOSTNAME_URL environment variable not set.");
            host = System.getenv("COMPUTERNAME");
            if (host != null) {
                return "http://" + host + ":" + routerPort + "/";
            }
            log.warn("COMPUTERNAME environment variable not set");
            return "http://localhost:" +serverPort + "/";
        }
        return host;
    }


    @Bean
    public ExportController.FilenameStrategy exportNameStrategy() {
        return () -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");
            try (Connection c = ds.getConnection()){
                return c.getCatalog() + "_" + format.format(new Date(System.currentTimeMillis()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
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
