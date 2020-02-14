package za.co.imqs.coreservice.audit;

import java.util.List;

public interface AuditLogger {
    public enum Operation {
        ADD,DELETE,UPDATE,READ,LOGIN,LOGOUT,GRANT,REVOKE
    }
    public enum Result {
        SUCCESS,FAILURE
    }

    public void log(AuditLogEntry entry);
    public void log(List<AuditLogEntry> entries);
}