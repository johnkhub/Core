package za.co.imqs.coreservice.audit;

import za.co.imqs.coreservice.dataaccess.AuditLogWriter;

import java.util.List;
import java.util.UUID;

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
        r.setAudit_id(UUID.fromString(entry.getAffectedEntity()));
        r.setEvent_time(entry.getAt());
        r.setPrincipal_id(UUID.fromString(entry.getUserId()));
        r.setAction(entry.getOperation().toString());
        r.setStatus(entry.getResult().toString());

        return r;
    }
}
