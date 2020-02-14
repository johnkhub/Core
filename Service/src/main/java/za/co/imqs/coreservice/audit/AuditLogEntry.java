package za.co.imqs.coreservice.audit;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2016/09/30
 */
@Getter
public class AuditLogEntry {
    private final Timestamp at;
    private final String userId;

    private final String affectedEntity;
    private final AuditLogger.Operation operation;
    private final String parameters;
    private final String entityDescription;

    private AuditLogger.Result result;

    public AuditLogEntry(
            String userId,
            String entityDescription,
            String affectedEntity,
            AuditLogger.Operation operation, String parameters,
            AuditLogger.Result result) {
       this(new Timestamp(System.currentTimeMillis()), userId, entityDescription, affectedEntity, operation, parameters, result);
    }

    public AuditLogEntry(
            String userId,
            String entityDescription,
            String affectedEntity,
            AuditLogger.Operation operation, String parameters
    ){
        this(new Timestamp(System.currentTimeMillis()), userId, entityDescription, affectedEntity, operation, parameters, null);
    }

    public AuditLogEntry(
            Timestamp at,
            String user,
            String entityDescription,
            String affectedEntity,
            AuditLogger.Operation operation, String parameters,
            AuditLogger.Result result
    ) {
        this.userId = user;
        this.affectedEntity = affectedEntity;
        this.entityDescription = entityDescription;
        this.operation = operation;
        this.parameters = parameters;
        this.result = result;
        this.at = at;
    }

    public void setStatus(AuditLogger.Result result) {
        this.result = result;
    }

    public String toString() {
        return String.format(
                "%s : %s attempt to %s %s %s with %s. %s",
                at.toString(), userId, operation, entityDescription, affectedEntity, parameters, result
        );
    }

    public boolean equals(Object o) {
        if (o instanceof AuditLogEntry) {
            final AuditLogEntry other = (AuditLogEntry)o;
            return
                    this.getAffectedEntity().equals(other.getAffectedEntity()) &&
                    this.getEntityDescription().equals(other.getEntityDescription()) &&
                    this.getResult() == other.getResult() &&
                    this.getOperation() == other.getOperation() &&
                    this.getAt().equals(other.getAt()) &&
                    this.getUserId().equals(other.getUserId()) &&
                    this.getParameters().equals(other.getParameters());
        }
        return false;
    }

    public int hashCode() {
        return (at + affectedEntity).hashCode();
    }
}
