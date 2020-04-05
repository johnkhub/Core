package za.co.imqs.coreservice.audit;

import lombok.extern.slf4j.Slf4j;
import za.co.imqs.coreservice.ServiceConfiguration;

import static za.co.imqs.coreservice.ServiceConfiguration.Features.AUDIT_GLOBAL;
import static za.co.imqs.coreservice.audit.AuditLogger.Result.FAILURE;
import static za.co.imqs.coreservice.audit.AuditLogger.Result.SUCCESS;

/**
 * (c) 2019 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2019/03/04
 */
@Slf4j
public class AuditLoggingProxy {

    public interface TryWithAudit {
        public Object tryIt();
    }

    private final AuditLogger audit;

    public AuditLoggingProxy(AuditLogger audit) {
        this.audit = audit;
    }

    public Object tryIt(AuditLogEntry entry, TryWithAudit toTry) {
        entry.setStatus(FAILURE);
        try {
            final Object returned = toTry.tryIt();
            entry.setStatus(SUCCESS);
            return returned;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (AUDIT_GLOBAL.isActive()) {
                audit.log(entry); // WE PROBABLY NEED A FALLBACK HERE THAT WILL WRITE TO LOCAL FILESYSTEM?
            }
        }
    }
}
