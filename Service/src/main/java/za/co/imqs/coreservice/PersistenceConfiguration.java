package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import za.co.imqs.configuration.client.ConfigClient;
import za.co.imqs.coreservice.model.ORM;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;

import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
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
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@EnableTransactionManagement
@EnableRetry
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
        names.sort((a,b)-> a.charAt(0)-b.charAt(0));
        return names.toArray(new String[]{});
    }

    @Bean
    @Qualifier("core_retry")
    public RetryTemplate getCoreRetry() {
        return RetryConfigFactory.getSimpleFixedBackoffPolicy(3, 10000);
    }

    @Bean
    @Qualifier("lookup_retry")
    public RetryTemplate getLookupRetry() {
        return RetryConfigFactory.getSimpleFixedBackoffPolicy(3, 10000);
    }
}
