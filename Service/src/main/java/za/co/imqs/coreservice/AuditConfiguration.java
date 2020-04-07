package za.co.imqs.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggerImpl;
import za.co.imqs.coreservice.dataaccess.AuditLogWriterImpl;

import javax.sql.DataSource;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2019 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2019/02/15
 */
@Configuration
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
public class AuditConfiguration {


    private final ObjectMapper mapper;
    private final DataSource ds;

    @Autowired
    public AuditConfiguration(
            @Qualifier("core_ds") DataSource ds,
            ObjectMapper mapper
    ) {
        this.ds = ds;
        this.mapper = mapper;
    }

    @Bean
    @Qualifier("audit_ds")
    public DataSource getAuditDataSource() {
        return ds;
    }


    @Bean
    @Qualifier("audit_tx_mgr")
    public PlatformTransactionManager getAuditTransactionManager() {
        return new DataSourceTransactionManager(getAuditDataSource());
    }

    @Bean
    public AuditLogger getAuditLogger() {
        return new AuditLoggerImpl(new AuditLogWriterImpl(ds), mapper);
    }

}
