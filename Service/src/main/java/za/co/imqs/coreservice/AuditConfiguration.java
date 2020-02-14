package za.co.imqs.coreservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggerImpl;
import za.co.imqs.coreservice.dataaccess.AuditLogWriter;

/**
 * (c) 2019 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2019/02/15
 */
@Configuration
public class AuditConfiguration {

    @Bean
    public AuditLogger getAuditLogger() {
        return new AuditLoggerImpl(AuditLogWriter.NULL_AUDIT_WRITER);
    }
}
