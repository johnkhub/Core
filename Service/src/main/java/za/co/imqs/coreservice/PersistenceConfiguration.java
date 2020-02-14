package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import za.co.imqs.configuration.client.ConfigClient;
import za.co.imqs.libimqs.dbutils.DatabaseUtil;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import javax.sql.DataSource;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * Support for the various persistence repositories.
 * <p>
 * (c) 2015 IMQS Software
 * <p/>
 * User: FrankVR
 * Date: 2015/09/15
 */
@Slf4j
@Configuration
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@EnableTransactionManagement
public class PersistenceConfiguration {

    @Autowired
    private ConfigClient configClient;

    @Bean
    public DataSource getDataSource() {
        final DataSource dataSource = HikariCPClientConfigDatasourceHelper.getDataSource(configClient, "jdbc");
        DatabaseUtil.updateDb(dataSource, DatabaseUtil.DEFAULT_CHANGELOG, true);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager getTransactionManager() {
        return new DataSourceTransactionManager(getDataSource());
    }
}
