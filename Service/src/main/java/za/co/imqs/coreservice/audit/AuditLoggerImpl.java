package za.co.imqs.coreservice.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.imqs.coreservice.Benchmark;
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
    private final ObjectMapper mapper;

    public AuditLoggerImpl(
            AuditLogWriter writer,
            ObjectMapper mapper
    ) {
        this.writer = writer;
        this.mapper = mapper;
    }

    @Override
    public void log(AuditLogEntry entry) {
        try {
            Benchmark.get().get("Log").m(
                    ()-> writer.write(map(mapper, entry))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(List<AuditLogEntry> entries) {
        entries.forEach((e) -> log(e));
    }

    private static AuditLogWriter.AuditLogRow map(ObjectMapper mapper, AuditLogEntry entry)  {
        try {
            final AuditLogWriter.AuditLogRow r = new AuditLogWriter.AuditLogRow();
            r.setAudit_id(UUID.randomUUID());
            r.setEvent_time(entry.getAt());
            r.setPrincipal_id(entry.getUserId());
            r.setAction(entry.getOperation().toString());
            r.setStatus(entry.getResult().toString());
            r.setParameters(mapper.writeValueAsString(entry.getParameters()));
            r.setCorrelation(entry.getCorrelationId());
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
