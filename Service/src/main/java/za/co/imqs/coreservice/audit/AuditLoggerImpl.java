package za.co.imqs.coreservice.audit;

import za.co.imqs.coreservice.dataaccess.AuditLogWriter;

import java.util.List;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/06
 */
public class AuditLoggerImpl implements AuditLogger {

    private final AuditLogWriter writer;

    public AuditLoggerImpl(AuditLogWriter writer) {
        this.writer = writer;
    }

    @Override
    public void log(AuditLogEntry entry) {
        writer.write(new AuditLogWriter.AuditLogRow());
    }

    @Override
    public void log(List<AuditLogEntry> entries) {
        entries.stream().forEach((e) -> log(e));
    }

    private static AuditLogWriter.AuditLogRow map(AuditLogEntry entry) {
        final AuditLogWriter.AuditLogRow r = new AuditLogWriter.AuditLogRow();
        /*
        r.setAction("");
        r.setAudit_id();
        r.setEvent_time();
        r.setPrincipal_id();
        r.setStatus();

         */
        return r;
    }
}
