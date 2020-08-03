package za.co.imqs.coreservice.audit;

import lombok.Getter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2016/09/30
 */
@Getter
public class AuditLogEntry {
    private final Timestamp at;
    private final UUID userId;
    private final AuditLogger.Operation operation;
    private final Object parameters;

    private AuditLogger.Result result;
    private UUID correlationId;

    public AuditLogEntry(
            UUID userId,
            AuditLogger.Operation operation, Object parameters,
            AuditLogger.Result result) {
       this(new Timestamp(System.currentTimeMillis()), userId, operation, parameters, result);
    }

    public AuditLogEntry(
            UUID userId,
            AuditLogger.Operation operation,
            Object parameters
    ){
        this(new Timestamp(System.currentTimeMillis()), userId, operation, parameters, null);
    }

    public AuditLogEntry(
            Timestamp at,
            UUID user,
            AuditLogger.Operation operation,
            Object parameters,
            AuditLogger.Result result
    ) {
        if (user == null) throw new IllegalArgumentException("User UUID is not specified!");
        this.userId = user;
        this.operation = operation;
        this.parameters = parameters;
        this.result = result;
        this.at = at;
    }

    public void setStatus(AuditLogger.Result result) {
        this.result = result;
    }

    public AuditLogEntry setCorrelationId(UUID id) {
        this.correlationId = id;
        return this;
    }

    public String toString() {
        return String.format(
                "%s : %s attempted to %s %s. %s",
                at.toString(), userId, operation, parameters.toString(), result
        );
    }

    public boolean equals(Object o) {
        if (o instanceof AuditLogEntry) {
            final AuditLogEntry other = (AuditLogEntry)o;
            return
                    this.getResult() == other.getResult() &&
                    this.getOperation() == other.getOperation() &&
                    this.getAt().equals(other.getAt()) &&
                    this.getUserId().equals(other.getUserId()) &&
                    this.getParameters().equals(other.getParameters());
        }
        return false;
    }

    public int hashCode() {
        return at.hashCode() << 16 & userId.hashCode();
    }



    // Not terribly pretty - this functionality also exists as of Java 9.
    public static <T>  Map<String,T> of (
            String k1, T v1
    ) {
        return of(
                k1,v1,
                null, null
        );
    }

    public static <T>  Map<String,T> of (
            String k1, T v1,
            String k2, T v2
    ) {
        return of(
                k1,v1,
                k2, v2,
                null, null
        );
    }

    public static <T>  Map<String,T> of (
            String k1, T v1,
            String k2, T v2,
            String k3, T v3

    ) {
        return of(
                k1,v1,
                k2, v2,
                k3, v3,
                null, null
        );
    }

    public static <T>  Map<String,T> of (
            String k1, T v1,
            String k2, T v2,
            String k3, T v3,
            String k4, T v4
    ) {
        return of(
                k1,v1,
                k2, v2,
                k3, v3,
                k4, v4,
                null, null
        );
    }

    public static <T>  Map<String,T> of (
            String k1, T v1,
            String k2, T v2,
            String k3, T v3,
            String k4, T v4 ,
            String k5, T v5
    ) {
        final Map<String,T> map = new HashMap<>();
        if (k1 != null) {
            map.put(k1,v1);
        } else {
            return map;
        }

        if (k2 != null) {
            map.put(k2,v2);
        } else {
            return map;
        }

        if (k3 != null) {
            map.put(k3,v3);
        } else {
            return map;
        }

        if (k4 != null) {
            map.put(k4,v4);
        } else {
            return map;
        }

        if (k5 != null) {
            map.put(k5,v5);
        } else {
            return map;
        }

        return map;
    }
}
