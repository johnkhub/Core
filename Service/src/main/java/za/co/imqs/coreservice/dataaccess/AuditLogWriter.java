package za.co.imqs.coreservice.dataaccess;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public interface AuditLogWriter {
    public static final AuditLogWriter NULL_AUDIT_WRITER = new AuditLogWriter() {
        public void write(Collection<AuditLogRow> rows) {}
        public void write(AuditLogRow ...rows) {}
    };

    @Data
    public static class AuditLogRow {
        private UUID audit_id;
        private UUID principal_id;

        private Timestamp insert_time; // DEFAULT CURRENT_TIMESTAMP
        private Timestamp event_time;

        private String action;
        private String status;
        private UUID correlation;
    }

    public void write(Collection<AuditLogRow> rows);

    default void write(AuditLogRow ...rows) {
        write(Arrays.asList(rows));
    }

}
