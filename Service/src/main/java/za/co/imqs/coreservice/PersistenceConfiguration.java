package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import za.co.imqs.configuration.client.ConfigClient;
import za.co.imqs.coreservice.model.ORM;
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
    private final ConfigClient configClient;

    @SuppressWarnings("InstantiationOfUtilityClass")
    private final ORM orm = new ORM();

    @Autowired
    public PersistenceConfiguration(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @Bean
    @Qualifier("core_ds")
    public DataSource getCoreDataSource() {
        return HikariCPClientConfigDatasourceHelper.getDataSource(configClient, "jdbc");
    }

    @Bean
    @Qualifier("core_tx_mgr")
    public PlatformTransactionManager getCoreTransactionManager() {
        return new DataSourceTransactionManager(getCoreDataSource());
    }

    @Bean
    @Qualifier("lookup_ds")
    public DataSource getLookupDataSource() {
        return getCoreDataSource();
    }

    @Bean
    @Qualifier("lookup_tx_mgr")
    public PlatformTransactionManager getLookupTransactionManager() {
        return new DataSourceTransactionManager(getLookupDataSource());
    }

    @Bean
    @Qualifier("schemas")
    // Get the list of schema file names (in this case from the classpath)
    public String[] getSchemas() throws Exception {
        final List<String> names = new LinkedList<>();
        for (Resource resource : new PathMatchingResourcePatternResolver().getResources("classpath:*changelog_**"))   {
            names.add(resource.getFilename());
        }
        return names.toArray(new String[]{});
    }
}
